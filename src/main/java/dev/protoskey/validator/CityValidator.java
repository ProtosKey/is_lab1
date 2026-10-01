package dev.protoskey.validator;

import dev.protoskey.entity.Climate;
import dev.protoskey.entity.StandardOfLiving;
import dev.protoskey.exception.InvalidFieldException;
import dev.protoskey.model.CityDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@ApplicationScoped
public class CityValidator extends AbstractValidator<CityDto> {
    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    protected FieldCheck[] checksFor(CityDto dto) {
        return new FieldCheck[] {
                () -> validateName(dto.getName()),
                () -> validateArea(dto.getArea()),
                () -> validatePopulation(dto.getPopulation()),
                () -> validatePopulationDensity(dto.getPopulationDensity()),
                () -> validateCoordinatesId(dto.getCoordinatesId()),
                () -> validateGovernorId(dto.getGovernorId()),
                () -> validateClimate(dto.getClimate()),
                () -> validateStandardOfLiving(dto.getStandardOfLiving()),
                () -> validateEstablishmentDate(dto.getEstablishmentDate())
        };
    }

    private void validateName(String name) throws InvalidFieldException {
        requireNotBlank("name", name, "Название не может быть пустым");
    }

    private void validateArea(Integer area) throws InvalidFieldException {
        requirePositive("area", area, "Площадь должна быть больше 0");
    }

    private void validatePopulation(Integer population) throws InvalidFieldException {
        requirePositive("population", population, "Население должно быть больше 0");
    }

    private void validatePopulationDensity(Float density) throws InvalidFieldException {
        requirePositive("populationDensity", density, "Плотность населения должна быть больше 0");
    }

    private void validateCoordinatesId(Integer id) throws InvalidFieldException {
        if (id == null) {
            throw new InvalidFieldException("coordinatesId", "Выберите координаты");
        }
    }

    private void validateGovernorId(Integer id) throws InvalidFieldException {
        if (id == null) {
            throw new InvalidFieldException("governorId", "Выберите губернатора");
        }
    }

    private void validateClimate(String climate) throws InvalidFieldException {
        if (climate == null || climate.isBlank()) {
            return;
        }
        boolean known = Arrays.stream(Climate.values()).anyMatch(v -> v.name().equals(climate));
        if (!known) {
            throw new InvalidFieldException("climate", "Неизвестный климат: " + climate);
        }
    }

    private void validateStandardOfLiving(String standard) throws InvalidFieldException {
        requireNotBlank("standardOfLiving", standard, "Выберите уровень жизни");
        boolean known = Arrays.stream(StandardOfLiving.values())
                .anyMatch(v -> v.name().equals(standard));
        if (!known) {
            throw new InvalidFieldException("standardOfLiving", "Неизвестный уровень жизни: " + standard);
        }
    }

    private void validateEstablishmentDate(String date) throws InvalidFieldException {
        if (date == null || date.isBlank()) {
            return;
        }
        try {
            LocalDateTime.parse(date, LOCAL);
        } catch (DateTimeParseException e) {
            throw new InvalidFieldException("establishmentDate", "Некорректная дата основания");
        }
    }
}
