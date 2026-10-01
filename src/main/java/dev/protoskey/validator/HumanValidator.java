package dev.protoskey.validator;

import dev.protoskey.exception.InvalidFieldException;
import dev.protoskey.model.HumanDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class HumanValidator extends AbstractValidator<HumanDto> {
    @Override
    protected FieldCheck[] checksFor(HumanDto dto) {
        return new FieldCheck[] {
                () -> validateName(dto.getName()),
                () -> validateAge(dto.getAge()),
                () -> validateBirthday(dto.getBirthday())
        };
    }

    private void validateName(String name) throws InvalidFieldException {
        requireNotBlank("name", name, "Имя не может быть пустым");
    }

    private void validateAge(Long age) throws InvalidFieldException {
        requirePositive("age", age, "Возраст должен быть больше 0");
    }

    private void validateBirthday(String birthday) throws InvalidFieldException {
        if (birthday == null || birthday.isBlank()) {
            return;
        }
        try {
            LocalDate.parse(birthday);
        } catch (DateTimeParseException e) {
            throw new InvalidFieldException("birthday", "Некорректная дата рождения");
        }
    }
}
