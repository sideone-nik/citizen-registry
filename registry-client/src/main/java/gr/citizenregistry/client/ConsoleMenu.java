package gr.citizenregistry.client;

import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.CitizenUpdate;
import gr.citizenregistry.domain.Gender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Επαναληπτικό μενού. Εμφανίζεται ξανά όσο ο χρήστης δίνει έγκυρες επιλογές/τιμές.
 * Μη έγκυρη επιλογή μενού ή μη έγκυρες τιμές (HTTP 400 από την υπηρεσία) -> τερματισμός.
 * "Δεν βρέθηκε" (404) / "υπάρχει ήδη" (409) δεν είναι μη έγκυρες τιμές: εμφανίζεται μήνυμα και το μενού συνεχίζει.
 */
@Component
public class ConsoleMenu implements CommandLineRunner {

    private final RegistryClient client;
    private final Scanner in = new Scanner(System.in);
    private final PrintStream out = System.out;

    public ConsoleMenu(RegistryClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) {
        out.println("Πελάτης Μητρώου Πολιτών - υπηρεσία: " + client.getBaseUrl());
        boolean keepGoing = true;
        try {
            while (keepGoing) {
                printMenu();
                String choice = read("Επιλογή: ").trim();
                keepGoing = switch (choice) {
                    case "1" -> execute(this::insert);
                    case "2" -> execute(this::delete);
                    case "3" -> execute(this::update);
                    case "4" -> execute(this::search);
                    case "5" -> execute(this::show);
                    default -> {
                        out.println("Μη έγκυρη επιλογή '" + choice + "'.");
                        yield false;
                    }
                };
            }
        } catch (InputClosedException e) {
            out.println();
        }
        out.println("Τερματισμός προγράμματος.");
    }

    private void printMenu() {
        out.println();
        out.println("=========== ΜΗΤΡΩΟ ΠΟΛΙΤΩΝ ===========");
        out.println(" 1. Εισαγωγή εγγραφής");
        out.println(" 2. Διαγραφή εγγραφής");
        out.println(" 3. Ενημέρωση εγγραφής (ΑΦΜ / διεύθυνση)");
        out.println(" 4. Αναζήτηση");
        out.println(" 5. Εμφάνιση πολίτη");
        out.println(" Οποιαδήποτε άλλη τιμή: έξοδος");
        out.println("======================================");
    }

    // ------------------------------------------------------------------ λειτουργίες

    private boolean insert() {
        String id = read("ΑΤ (8 χαρακτήρες): ");
        String firstName = read("Όνομα: ");
        String lastName = read("Επίθετο: ");
        String genderText = read("Φύλο (M/F): ");
        Gender gender;
        try {
            gender = Gender.fromString(genderText);
        } catch (IllegalArgumentException e) {
            out.println("Σφάλμα: " + e.getMessage());
            return false;
        }
        String birthDate = read("Ημερομηνία γέννησης (ΗΗ-ΜΜ-ΕΕΕΕ): ");
        String afm = emptyToNull(read("ΑΦΜ (9 ψηφία, Enter για παράλειψη): "));
        String address = emptyToNull(read("Διεύθυνση κατοικίας (Enter για παράλειψη): "));

        Citizen created = client.create(new Citizen(id.trim(), firstName.trim(), lastName.trim(), gender,
                birthDate.trim(), afm, address));
        out.println("Η εγγραφή καταχωρήθηκε:");
        print(created);
        return true;
    }

    private boolean delete() {
        String id = read("ΑΤ πολίτη προς διαγραφή: ").trim();
        client.delete(id);
        out.println("Η εγγραφή με ΑΤ " + id + " διαγράφηκε.");
        return true;
    }

    private boolean update() {
        String id = read("ΑΤ πολίτη προς ενημέρωση: ").trim();
        Citizen current = client.get(id);
        print(current);
        out.println("(Enter = διατήρηση τρέχουσας τιμής, '-' = διαγραφή τιμής)");
        String afm = keepOrReplace(read("Νέο ΑΦΜ [" + nvl(current.getAfm()) + "]: "), current.getAfm());
        String address = keepOrReplace(read("Νέα διεύθυνση [" + nvl(current.getAddress()) + "]: "),
                current.getAddress());

        Citizen updated = client.update(id, new CitizenUpdate(afm, address));
        out.println("Η εγγραφή ενημερώθηκε:");
        print(updated);
        return true;
    }

    private boolean search() {
        out.println("Συμπληρώστε όσα κριτήρια θέλετε (Enter για παράλειψη).");
        Map<String, String> criteria = new LinkedHashMap<>();
        addCriterion(criteria, "idNumber", "ΑΤ: ");
        addCriterion(criteria, "firstName", "Όνομα: ");
        addCriterion(criteria, "lastName", "Επίθετο: ");
        addCriterion(criteria, "gender", "Φύλο (M/F): ");
        addCriterion(criteria, "birthDate", "Ημερομηνία γέννησης (ΗΗ-ΜΜ-ΕΕΕΕ): ");
        addCriterion(criteria, "afm", "ΑΦΜ: ");
        addCriterion(criteria, "address", "Διεύθυνση (τμήμα κειμένου): ");

        List<Citizen> results = client.search(criteria);
        out.println("Βρέθηκαν " + results.size() + " εγγραφές.");
        results.forEach(this::print);
        return true;
    }

    private boolean show() {
        String id = read("ΑΤ πολίτη: ").trim();
        print(client.get(id));
        return true;
    }

    // ------------------------------------------------------------------ βοηθητικά

    /** Εκτελεί μια λειτουργία· επιστρέφει false αν πρέπει να τερματιστεί το πρόγραμμα. */
    private boolean execute(Operation operation) {
        try {
            return operation.run();
        } catch (RegistryApiException e) {
            e.getMessages().forEach(m -> out.println("Σφάλμα (" + e.getStatus() + "): " + m));
            if (e.isInvalidInput()) {
                out.println("Δόθηκαν μη έγκυρες τιμές.");
                return false;
            }
            return true;
        } catch (ResourceAccessException e) {
            out.println("Αδυναμία σύνδεσης με την υπηρεσία στο " + client.getBaseUrl()
                    + " - βεβαιωθείτε ότι εκτελείται.");
            return true;
        }
    }

    private void addCriterion(Map<String, String> criteria, String key, String prompt) {
        String value = read(prompt);
        if (!value.isEmpty()) {
            criteria.put(key, value.trim().isEmpty() ? value : value.trim());
        }
    }

    private String read(String prompt) {
        out.print(prompt);
        out.flush();
        if (!in.hasNextLine()) {
            throw new InputClosedException();
        }
        return in.nextLine();
    }

    private static String keepOrReplace(String input, String current) {
        String v = input.trim();
        if (v.isEmpty()) {
            return current;
        }
        return "-".equals(v) ? null : v;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String nvl(String value) {
        return value == null ? "-" : value;
    }

    private void print(Citizen c) {
        out.printf("  ΑΤ: %s | %s %s | Φύλο: %s | Γέννηση: %s | ΑΦΜ: %s | Διεύθυνση: %s%n",
                c.getIdNumber(), c.getFirstName(), c.getLastName(), c.getGender(), c.getBirthDate(),
                nvl(c.getAfm()), nvl(c.getAddress()));
    }

    @FunctionalInterface
    private interface Operation {
        boolean run();
    }

    private static final class InputClosedException extends RuntimeException {
    }
}
