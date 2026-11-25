package gr.citizenregistry.domain;

import java.util.List;

/** Μορφή μηνύματος λάθους που επιστρέφει η υπηρεσία (και διαβάζει ο πελάτης). */
public record ApiError(String timestamp, int status, String error, List<String> messages) {
}
