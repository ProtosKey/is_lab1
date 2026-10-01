package dev.protoskey.exception;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String what, Integer id) {
        super(what + " с id " + id + " не найден");
    }
}
