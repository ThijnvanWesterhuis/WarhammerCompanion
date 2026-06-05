package backend.api.exception;

import java.util.Map;

public class FieldValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public FieldValidationException(String field, String message) {
        super(message);
        this.errors = Map.of(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}