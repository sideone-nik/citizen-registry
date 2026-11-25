package gr.citizenregistry.service.exception;

import java.util.List;

public class InvalidInputException extends RuntimeException {

    private final List<String> errors;

    public InvalidInputException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public InvalidInputException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
