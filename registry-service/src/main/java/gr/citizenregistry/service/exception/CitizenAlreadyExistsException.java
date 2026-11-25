package gr.citizenregistry.service.exception;

public class CitizenAlreadyExistsException extends RuntimeException {

    public CitizenAlreadyExistsException(String idNumber) {
        super("Υπάρχει ήδη εγγραφή πολίτη με ΑΤ '" + idNumber + "'");
    }
}
