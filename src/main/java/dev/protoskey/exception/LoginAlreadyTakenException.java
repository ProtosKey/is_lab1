package dev.protoskey.exception;

public class LoginAlreadyTakenException extends Exception {
    public LoginAlreadyTakenException(String login) {
        super("Пользователь с логином " + login + " уже существует");
    }
}
