package dev.protoskey.validator;

import dev.protoskey.exception.ValidationException;

public interface Validator<T> {
    void validate(T target) throws ValidationException;
}
