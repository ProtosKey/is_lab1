package dev.protoskey.exception;

public class PopulationOverflowException extends Exception {
    public PopulationOverflowException() {
        super("Суммарное население не помещается в тип поля");
    }
}
