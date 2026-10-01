package dev.protoskey.validator;

import dev.protoskey.exception.InvalidFieldException;
import dev.protoskey.model.CredentialsDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialsValidator extends AbstractValidator<CredentialsDto> {
    @Override
    protected FieldCheck[] checksFor(CredentialsDto dto) {
        return new FieldCheck[] {
                () -> validateLogin(dto.getLogin()),
                () -> validatePassword(dto.getPassword())
        };
    }

    private void validateLogin(String login) throws InvalidFieldException {
        requireNotBlank("login", login, "Логин не может быть пустым");
    }

    private void validatePassword(String password) throws InvalidFieldException {
        requireNotBlank("password", password, "Пароль не может быть пустым");
    }
}
