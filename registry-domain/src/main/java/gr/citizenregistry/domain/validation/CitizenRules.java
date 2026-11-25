package gr.citizenregistry.domain.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Κοινοί κανόνες επικύρωσης των πεδίων του μητρώου.
 * Χρησιμοποιούνται από τα annotations των οντοτήτων και από τη λογική της υπηρεσίας.
 */
public final class CitizenRules {

    /** ΑΤ: ακριβώς 8 χαρακτήρες, γράμματα (λατινικά ή ελληνικά) ή ψηφία. */
    public static final String ID_NUMBER_REGEX = "^[\\p{L}\\p{N}]{8}$";

    /** ΑΦΜ: ακριβώς 9 ψηφία. */
    public static final String AFM_REGEX = "^[0-9]{9}$";

    /** Μορφή ημερομηνίας γέννησης: ΗΗ-ΜΜ-ΕΕΕΕ. */
    public static final String BIRTH_DATE_FORMAT_REGEX = "^[0-9]{2}-[0-9]{2}-[0-9]{4}$";

    public static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private CitizenRules() {
    }

    public static boolean isValidIdNumber(String idNumber) {
        return idNumber != null && idNumber.matches(ID_NUMBER_REGEX);
    }

    public static boolean isValidAfm(String afm) {
        return afm != null && afm.matches(AFM_REGEX);
    }

    /** Έγκυρη = σωστή μορφή ΗΗ-ΜΜ-ΕΕΕΕ, υπαρκτή ημερομηνία και όχι στο μέλλον. */
    public static boolean isValidBirthDate(String birthDate) {
        if (birthDate == null || !birthDate.matches(BIRTH_DATE_FORMAT_REGEX)) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(birthDate, BIRTH_DATE_FORMATTER);
            return !date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
