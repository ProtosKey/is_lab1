package dev.protoskey.mapper;

import dev.protoskey.entity.Human;
import dev.protoskey.model.HumanDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class HumanMapper implements Mapper<HumanDto, Human> {
    @Override
    public Human toEntity(HumanDto dto) {
        Human entity = new Human();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setAge(dto.getAge() == null ? 0 : dto.getAge());
        entity.setBirthday(toDate(dto.getBirthday()));
        return entity;
    }

    @Override
    public HumanDto toDto(Human entity) {
        HumanDto dto = new HumanDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAge(entity.getAge());
        dto.setBirthday(toText(entity.getBirthday()));
        return dto;
    }

    private Date toDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        LocalDate date = LocalDate.parse(text);
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String toText(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }
}
