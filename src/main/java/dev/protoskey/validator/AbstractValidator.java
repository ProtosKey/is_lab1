package dev.protoskey.validator;

import dev.protoskey.exception.InvalidFieldException;
import dev.protoskey.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractValidator<T> implements Validator<T> {
    @FunctionalInterface
    protected interface FieldCheck {
        void run() throws InvalidFieldException;
    }

    protected abstract FieldCheck[] checksFor(T target);

    @Override
    public void validate(T target) throws ValidationException {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldCheck check : checksFor(target)) {
            try {
                check.run();
            } catch (InvalidFieldException e) {
                errors.put(e.getField(), e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    protected void requireNotBlank(String field, String value, String message)
            throws InvalidFieldException {
        if (value == null || value.isBlank()) {
            throw new InvalidFieldException(field, message);
        }
    }

    protected void requirePositive(String field, Number value, String message)
            throws InvalidFieldException {
        if (value == null || value.doubleValue() <= 0) {
            throw new InvalidFieldException(field, message);
        }
    }
}
