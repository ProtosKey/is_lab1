package dev.protoskey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "city")
@Getter
@Setter
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Название не может быть пустым")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Координаты обязательны")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @NotNull(message = "Площадь обязательна")
    @Positive(message = "Площадь должна быть больше 0")
    @Column(name = "area", nullable = false, columnDefinition = "integer not null check (area > 0)")
    private Integer area;

    @Positive(message = "Население должно быть больше 0")
    @Column(name = "population", nullable = false, columnDefinition = "integer not null check (population > 0)")
    private int population;

    @Column(name = "establishment_date", columnDefinition = "timestamp with time zone")
    private ZonedDateTime establishmentDate;

    @Column(name = "capital", nullable = false)
    private boolean capital;

    @Column(name = "meters_above_sea_level", columnDefinition = "real")
    private Float metersAboveSeaLevel;

    @Positive(message = "Плотность населения должна быть больше 0")
    @Column(name = "population_density", nullable = false,
            columnDefinition = "real not null check (population_density > 0)")
    private float populationDensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "climate")
    private Climate climate;

    @NotNull(message = "Уровень жизни обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "standard_of_living", nullable = false)
    private StandardOfLiving standardOfLiving;

    @NotNull(message = "Губернатор обязателен")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "governor_id", nullable = false)
    private Human governor;

    @PrePersist
    private void fillCreationDate() {
        creationDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof City that && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
