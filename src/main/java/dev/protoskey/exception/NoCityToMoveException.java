package dev.protoskey.exception;

public class NoCityToMoveException extends Exception {
    public NoCityToMoveException() {
        super("Не нашлось другого города, в который можно переселить население");
    }
}
