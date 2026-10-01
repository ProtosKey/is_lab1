package dev.protoskey.service;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Coordinates;
import dev.protoskey.entity.Human;
import dev.protoskey.event.CityChanged;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.repository.CityQuery;
import dev.protoskey.repository.CityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CityService {
    @Inject
    private CityRepository cityRepository;

    @Inject
    private ReferenceService referenceService;

    @Inject
    private Event<CityChanged> cityChanged;

    public List<City> findPage(CityQuery query) {
        return cityRepository.findPage(query);
    }

    public long count(CityQuery query) {
        return cityRepository.countAll(query);
    }

    public City findById(Integer id) throws EntityNotFoundException {
        return cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Город", id));
    }

    @Transactional(rollbackOn = Exception.class)
    public City create(City city, Integer coordinatesId, Integer governorId)
            throws EntityNotFoundException {
        linkReferences(city, coordinatesId, governorId);
        City saved = cityRepository.save(city);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public City update(Integer id, City changes, Integer coordinatesId, Integer governorId)
            throws EntityNotFoundException {
        City existing = findById(id);
        existing.setName(changes.getName());
        existing.setArea(changes.getArea());
        existing.setPopulation(changes.getPopulation());
        existing.setEstablishmentDate(changes.getEstablishmentDate());
        existing.setCapital(changes.isCapital());
        existing.setMetersAboveSeaLevel(changes.getMetersAboveSeaLevel());
        existing.setPopulationDensity(changes.getPopulationDensity());
        existing.setClimate(changes.getClimate());
        existing.setStandardOfLiving(changes.getStandardOfLiving());
        linkReferences(existing, coordinatesId, governorId);

        City saved = cityRepository.update(existing);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public void delete(Integer id) throws EntityNotFoundException {
        cityRepository.delete(findById(id));
        cityChanged.fire(new CityChanged());
    }

    private void linkReferences(City city, Integer coordinatesId, Integer governorId)
            throws EntityNotFoundException {
        Coordinates coordinates = referenceService.findCoordinates(coordinatesId);
        Human governor = referenceService.findHuman(governorId);
        city.setCoordinates(coordinates);
        city.setGovernor(governor);
    }
}
