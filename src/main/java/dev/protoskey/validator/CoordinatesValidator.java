package dev.protoskey.validator;

import dev.protoskey.exception.InvalidFieldException;
import dev.protoskey.model.CoordinatesDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoordinatesValidator extends AbstractValidator<CoordinatesDto> {
    private static final int MAX_X = 974;

    @Override
    protected FieldCheck[] checksFor(CoordinatesDto dto) {
        return new FieldCheck[] {
                () -> validateX(dto.getX()),
                () -> validateY(dto.getY())
        };
    }

    private void validateX(Integer x) throws InvalidFieldException {
        if (x == null) {
            throw new InvalidFieldException("x", "Укажите координату x");
        }
        if (x > MAX_X) {
            throw new InvalidFieldException("x", "Координата x не может быть больше " + MAX_X);
        }
    }

    private void validateY(Integer y) throws InvalidFieldException {
        if (y == null) {
            throw new InvalidFieldException("y", "Укажите координату y");
        }
    }
}
