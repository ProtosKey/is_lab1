package dev.protoskey.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends Exception {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("Данные заполнены неверно");
        this.fieldErrors = fieldErrors;
    }
}
