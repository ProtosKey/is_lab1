package dev.protoskey.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityDto {
    private Integer id;
    private String name;
    private Integer coordinatesId;
    private Integer coordinatesX;
    private Integer coordinatesY;
    private String creationDate;
    private Integer area;
    private Integer population;
    private String establishmentDate;
    private Boolean capital;
    private Float metersAboveSeaLevel;
    private Float populationDensity;
    private String climate;
    private String standardOfLiving;
    private Integer governorId;
    private String governorName;
    private Long governorAge;
    private String governorBirthday;
}
