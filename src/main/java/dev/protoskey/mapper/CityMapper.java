package dev.protoskey.mapper;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Climate;
import dev.protoskey.entity.StandardOfLiving;
import dev.protoskey.model.CityDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class CityMapper implements Mapper<CityDto, City> {
    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    public City toEntity(CityDto dto) {
        City entity = new City();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setArea(dto.getArea());
        entity.setPopulation(dto.getPopulation() == null ? 0 : dto.getPopulation());
        entity.setEstablishmentDate(toZoned(dto.getEstablishmentDate()));
        entity.setCapital(Boolean.TRUE.equals(dto.getCapital()));
        entity.setMetersAboveSeaLevel(dto.getMetersAboveSeaLevel());
        entity.setPopulationDensity(dto.getPopulationDensity() == null ? 0 : dto.getPopulationDensity());
        entity.setClimate(dto.getClimate() == null || dto.getClimate().isBlank()
                ? null
                : Climate.valueOf(dto.getClimate()));
        entity.setStandardOfLiving(dto.getStandardOfLiving() == null || dto.getStandardOfLiving().isBlank()
                ? null
                : StandardOfLiving.valueOf(dto.getStandardOfLiving()));
        return entity;
    }

    @Override
    public CityDto toDto(City entity) {
        CityDto dto = new CityDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setArea(entity.getArea());
        dto.setPopulation(entity.getPopulation());
        dto.setCapital(entity.isCapital());
        dto.setMetersAboveSeaLevel(entity.getMetersAboveSeaLevel());
        dto.setPopulationDensity(entity.getPopulationDensity());
        dto.setClimate(entity.getClimate() == null ? null : entity.getClimate().name());
        dto.setStandardOfLiving(entity.getStandardOfLiving() == null
                ? null
                : entity.getStandardOfLiving().name());

        dto.setCreationDate(entity.getCreationDate() == null
                ? null
                : entity.getCreationDate().format(LOCAL));
        dto.setEstablishmentDate(entity.getEstablishmentDate() == null
                ? null
                : entity.getEstablishmentDate().toLocalDateTime().format(LOCAL));

        if (entity.getCoordinates() != null) {
            dto.setCoordinatesId(entity.getCoordinates().getId());
            dto.setCoordinatesX(entity.getCoordinates().getX());
            dto.setCoordinatesY(entity.getCoordinates().getY());
        }
        if (entity.getGovernor() != null) {
            dto.setGovernorId(entity.getGovernor().getId());
            dto.setGovernorName(entity.getGovernor().getName());
            dto.setGovernorAge(entity.getGovernor().getAge());
        }
        return dto;
    }

    private ZonedDateTime toZoned(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(text, LOCAL).atZone(ZoneId.systemDefault());
    }
}
