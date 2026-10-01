package dev.protoskey.exception;

public class ReplacementRequiredException extends Exception {
    public ReplacementRequiredException(String what) {
        super("Выберите " + what + ", к которым перейдут связанные города");
    }
}
