package gr.citizenregistry.service.service;

import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.CitizenUpdate;
import gr.citizenregistry.domain.Gender;
import gr.citizenregistry.domain.validation.CitizenRules;
import gr.citizenregistry.service.exception.CitizenAlreadyExistsException;
import gr.citizenregistry.service.exception.CitizenNotFoundException;
import gr.citizenregistry.service.exception.InvalidInputException;
import gr.citizenregistry.service.repository.CitizenRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CitizenService {

    private final CitizenRepository repository;
    private final Validator validator;

    public CitizenService(CitizenRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /** Εισαγωγή: επικύρωση πεδίων, έλεγχος ύπαρξης, αποθήκευση. */
    public Citizen create(Citizen citizen) {
        if (citizen == null) {
            throw new InvalidInputException("Δεν δόθηκαν στοιχεία πολίτη");
        }
        citizen.setAfm(blankToNull(citizen.getAfm()));
        citizen.setAddress(blankToNull(citizen.getAddress()));
        validateOrThrow(citizen);
        if (repository.existsById(citizen.getIdNumber())) {
            throw new CitizenAlreadyExistsException(citizen.getIdNumber());
        }
        return repository.save(citizen);
    }

    /** Εμφάνιση με βάση τον ΑΤ. */
    @Transactional(readOnly = true)
    public Citizen getByIdNumber(String idNumber) {
        requireValidIdNumber(idNumber);
        return repository.findById(idNumber).orElseThrow(() -> new CitizenNotFoundException(idNumber));
    }

    /** Ενημέρωση μόνο ΑΦΜ και διεύθυνσης. */
    public Citizen update(String idNumber, CitizenUpdate update) {
        requireValidIdNumber(idNumber);
        if (update == null) {
            throw new InvalidInputException("Δεν δόθηκαν στοιχεία ενημέρωσης");
        }
        update.setAfm(blankToNull(update.getAfm()));
        update.setAddress(blankToNull(update.getAddress()));
        validateOrThrow(update);

        Citizen existing = repository.findById(idNumber)
                .orElseThrow(() -> new CitizenNotFoundException(idNumber));
        existing.setAfm(update.getAfm());
        existing.setAddress(update.getAddress());
        return repository.save(existing);
    }

    /** Διαγραφή με βάση τον ΑΤ. */
    public void delete(String idNumber) {
        requireValidIdNumber(idNumber);
        if (!repository.existsById(idNumber)) {
            throw new CitizenNotFoundException(idNumber);
        }
        repository.deleteById(idNumber);
    }

    /**
     * Αναζήτηση με οποιονδήποτε συνδυασμό πεδίων (AND). Όνομα/επίθετο: ακριβές ταίριασμα χωρίς
     * διάκριση πεζών-κεφαλαίων. Διεύθυνση: περιέχει (χωρίς διάκριση πεζών-κεφαλαίων).
     * Χωρίς κριτήρια επιστρέφονται όλες οι εγγραφές.
     */
    @Transactional(readOnly = true)
    public List<Citizen> search(SearchCriteria criteria) {
        SearchCriteria c = criteria == null ? SearchCriteria.empty() : criteria;
        List<String> errors = new ArrayList<>();

        if (c.idNumber() != null && !CitizenRules.isValidIdNumber(c.idNumber())) {
            errors.add("Ο αριθμός ταυτότητας (ΑΤ) πρέπει να αποτελείται από ακριβώς 8 χαρακτήρες (γράμματα ή ψηφία)");
        }
        if (c.firstName() != null && c.firstName().isBlank()) {
            errors.add("Το όνομα δεν μπορεί να είναι κενό");
        }
        if (c.lastName() != null && c.lastName().isBlank()) {
            errors.add("Το επίθετο δεν μπορεί να είναι κενό");
        }
        Gender gender = null;
        if (c.gender() != null) {
            try {
                gender = Gender.fromString(c.gender());
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        if (c.birthDate() != null && !CitizenRules.isValidBirthDate(c.birthDate())) {
            errors.add("Η ημερομηνία γέννησης πρέπει να είναι έγκυρη και της μορφής ΗΗ-ΜΜ-ΕΕΕΕ (π.χ. 12-11-2008)");
        }
        if (c.afm() != null && !CitizenRules.isValidAfm(c.afm())) {
            errors.add("Το ΑΦΜ πρέπει να αποτελείται από ακριβώς 9 ψηφία");
        }
        if (c.address() != null && c.address().isBlank()) {
            errors.add("Η διεύθυνση δεν μπορεί να είναι κενή");
        }
        if (!errors.isEmpty()) {
            throw new InvalidInputException(errors);
        }

        Citizen probe = new Citizen(c.idNumber(), trim(c.firstName()), trim(c.lastName()), gender,
                c.birthDate(), c.afm(), trim(c.address()));
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.ignoreCase())
                .withMatcher("address", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        return repository.findAll(Example.of(probe, matcher), Sort.by("idNumber"));
    }

    private void requireValidIdNumber(String idNumber) {
        if (idNumber == null || idNumber.isBlank()) {
            throw new InvalidInputException("Ο αριθμός ταυτότητας (ΑΤ) είναι υποχρεωτικός");
        }
        if (!CitizenRules.isValidIdNumber(idNumber)) {
            throw new InvalidInputException(
                    "Ο αριθμός ταυτότητας (ΑΤ) πρέπει να αποτελείται από ακριβώς 8 χαρακτήρες (γράμματα ή ψηφία)");
        }
    }

    private <T> void validateOrThrow(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            List<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .sorted()
                    .toList();
            throw new InvalidInputException(errors);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
