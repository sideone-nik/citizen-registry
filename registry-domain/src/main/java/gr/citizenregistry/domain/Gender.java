package gr.citizenregistry.domain;

import java.util.Locale;

/** Φύλο πολίτη. Στο JSON χρησιμοποιούνται οι τιμές MALE / FEMALE. */
public enum Gender {
    MALE,
    FEMALE;

    /**
     * Μετατροπή κειμένου σε Gender. Δέχεται MALE/FEMALE ή M/F (χωρίς διάκριση πεζών/κεφαλαίων).
     *
     * @throws IllegalArgumentException αν η τιμή δεν είναι έγκυρη
     */
    public static Gender fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Το φύλο δεν μπορεί να είναι κενό");
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        return switch (v) {
            case "MALE", "M" -> MALE;
            case "FEMALE", "F" -> FEMALE;
            default -> throw new IllegalArgumentException(
                    "Μη έγκυρο φύλο '" + value + "' (επιτρεπτές τιμές: MALE, FEMALE, M, F)");
        };
    }
}
