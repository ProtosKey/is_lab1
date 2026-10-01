package dev.protoskey.mapper;

import dev.protoskey.entity.Coordinates;
import dev.protoskey.model.CoordinatesDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoordinatesMapper implements Mapper<CoordinatesDto, Coordinates> {
    @Override
    public Coordinates toEntity(CoordinatesDto dto) {
        Coordinates entity = new Coordinates();
        entity.setId(dto.getId());
        entity.setX(dto.getX());
        entity.setY(dto.getY());
        return entity;
    }

    @Override
    public CoordinatesDto toDto(Coordinates entity) {
        CoordinatesDto dto = new CoordinatesDto();
        dto.setId(entity.getId());
        dto.setX(entity.getX());
        dto.setY(entity.getY());
        return dto;
    }
}
