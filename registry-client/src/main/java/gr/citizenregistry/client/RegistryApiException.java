package gr.citizenregistry.client;

import java.util.List;

/** Λάθος που επέστρεψε η RESTful υπηρεσία (κωδικός HTTP + μηνύματα). */
public class RegistryApiException extends RuntimeException {

    private final int status;
    private final List<String> messages;

    public RegistryApiException(int status, List<String> messages) {
        super("HTTP " + status + ": " + String.join("; ", messages));
        this.status = status;
        this.messages = List.copyOf(messages);
    }

    public int getStatus() {
        return status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean isInvalidInput() {
        return status == 400;
    }
}
