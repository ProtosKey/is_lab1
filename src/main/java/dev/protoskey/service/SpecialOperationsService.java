package dev.protoskey.service;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Climate;
import dev.protoskey.event.CityChanged;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.NoCityToMoveException;
import dev.protoskey.exception.NoCityWithClimateException;
import dev.protoskey.exception.PopulationOverflowException;
import dev.protoskey.exception.SameObjectException;
import dev.protoskey.repository.CityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class SpecialOperationsService {
    @Inject
    private CityRepository cityRepository;

    @Inject
    private Event<CityChanged> cityChanged;

    @Transactional(rollbackOn = Exception.class)
    public City deleteOneByClimate(Climate climate) throws NoCityWithClimateException {
        City city = cityRepository.findAnyByClimate(climate)
                .orElseThrow(() -> new NoCityWithClimateException(climate));
        cityRepository.delete(city);
        cityChanged.fire(new CityChanged());
        return city;
    }

    public long countByMetersAboveSeaLevel(Float metersAboveSeaLevel) {
        return cityRepository.countByMetersAboveSeaLevel(metersAboveSeaLevel);
    }

    public List<City> findByNameContains(String part) {
        return cityRepository.findByNameContains(part);
    }

    @Transactional(rollbackOn = Exception.class)
    public City movePopulation(Integer sourceId, Integer targetId)
            throws SameObjectException, EntityNotFoundException, PopulationOverflowException {
        if (sourceId.equals(targetId)) {
            throw new SameObjectException("Город-источник и город-получатель должны различаться");
        }
        return move(findCity(sourceId), findCity(targetId));
    }

    @Transactional(rollbackOn = Exception.class)
    public City movePopulationToSmallest(Integer sourceId)
            throws EntityNotFoundException, NoCityToMoveException, PopulationOverflowException {
        City source = findCity(sourceId);
        City target = cityRepository.findWithSmallestPopulationExcept(sourceId)
                .orElseThrow(NoCityToMoveException::new);
        return move(source, target);
    }

    private City move(City source, City target) throws PopulationOverflowException {
        int moved;
        try {
            moved = Math.addExact(target.getPopulation(), source.getPopulation());
        } catch (ArithmeticException e) {
            throw new PopulationOverflowException();
        }

        target.setPopulation(moved);
        cityRepository.update(target);
        cityRepository.delete(source);
        cityChanged.fire(new CityChanged());
        return target;
    }

    private City findCity(Integer id) throws EntityNotFoundException {
        return cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Город", id));
    }
}
