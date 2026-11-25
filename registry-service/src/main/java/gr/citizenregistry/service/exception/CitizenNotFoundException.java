package gr.citizenregistry.service.exception;

public class CitizenNotFoundException extends RuntimeException {

    public CitizenNotFoundException(String idNumber) {
        super("Δεν βρέθηκε πολίτης με ΑΤ '" + idNumber + "'");
    }
}
