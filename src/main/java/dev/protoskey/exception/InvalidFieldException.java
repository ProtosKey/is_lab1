package dev.protoskey.exception;

import lombok.Getter;

@Getter
public class InvalidFieldException extends Exception {
    private final String field;
    public InvalidFieldException(String field, String message) {
        super(message);
        this.field = field;
    }
}
