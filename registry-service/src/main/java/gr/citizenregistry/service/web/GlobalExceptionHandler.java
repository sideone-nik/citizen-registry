package gr.citizenregistry.service.web;

import gr.citizenregistry.domain.ApiError;
import gr.citizenregistry.service.exception.CitizenAlreadyExistsException;
import gr.citizenregistry.service.exception.CitizenNotFoundException;
import gr.citizenregistry.service.exception.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiError> handleInvalid(InvalidInputException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getErrors());
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(CitizenNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    @ExceptionHandler(CitizenAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleExists(CitizenAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, List.of(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, List.of(
                "Μη έγκυρο ή κενό σώμα αιτήματος JSON (π.χ. μη έγκυρη τιμή φύλου - επιτρεπτές: MALE, FEMALE)"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, List.of("Μη έγκυρη τιμή για την παράμετρο '" + ex.getName() + "'"));
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, List<String> messages) {
        ApiError body = new ApiError(OffsetDateTime.now().toString(), status.value(),
                status.getReasonPhrase(), messages);
        return ResponseEntity.status(status).body(body);
    }
}
