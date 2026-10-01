package dev.protoskey.exception;

public class AuthenticationException extends Exception {
    public AuthenticationException() {
        super("Неверный логин или пароль");
    }
}
