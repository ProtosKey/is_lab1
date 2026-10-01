package dev.protoskey.service;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Coordinates;
import dev.protoskey.entity.Human;
import dev.protoskey.event.CityChanged;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ReplacementRequiredException;
import dev.protoskey.exception.SameObjectException;
import dev.protoskey.repository.CityRepository;
import dev.protoskey.repository.CoordinatesRepository;
import dev.protoskey.repository.HumanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ReferenceService {
    @Inject
    private CoordinatesRepository coordinatesRepository;

    @Inject
    private HumanRepository humanRepository;

    @Inject
    private CityRepository cityRepository;

    @Inject
    private Event<CityChanged> cityChanged;

    public List<Coordinates> findAllCoordinates() {
        return coordinatesRepository.findAll();
    }

    public List<Human> findAllHumans() {
        return humanRepository.findAll();
    }

    public Coordinates findCoordinates(Integer id) throws EntityNotFoundException {
        return coordinatesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Координаты", id));
    }

    public Human findHuman(Integer id) throws EntityNotFoundException {
        return humanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Губернатор", id));
    }

    public List<City> citiesWithCoordinates(Integer id) {
        return cityRepository.findByCoordinates(id);
    }

    public List<City> citiesWithGovernor(Integer id) {
        return cityRepository.findByGovernor(id);
    }

    @Transactional(rollbackOn = Exception.class)
    public Coordinates createCoordinates(Coordinates coordinates) {
        Coordinates saved = coordinatesRepository.save(coordinates);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public Coordinates updateCoordinates(Integer id, Coordinates changes)
            throws EntityNotFoundException {

        Coordinates existing = findCoordinates(id);
        existing.setX(changes.getX());
        existing.setY(changes.getY());
        Coordinates saved = coordinatesRepository.update(existing);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public Human createHuman(Human human) {
        Human saved = humanRepository.save(human);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public Human updateHuman(Integer id, Human changes) throws EntityNotFoundException {
        Human existing = findHuman(id);
        existing.setName(changes.getName());
        existing.setAge(changes.getAge());
        existing.setBirthday(changes.getBirthday());
        Human saved = humanRepository.update(existing);
        cityChanged.fire(new CityChanged());
        return saved;
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteCoordinates(Integer id, Integer replacementId)
            throws EntityNotFoundException, SameObjectException, ReplacementRequiredException {
        Coordinates target = findCoordinates(id);
        List<City> linked = cityRepository.findByCoordinates(id);

        if (!linked.isEmpty()) {
            checkReplacement(id, replacementId, "координаты");
            Coordinates replacement = findCoordinates(replacementId);
            linked.forEach(city -> city.setCoordinates(replacement));
        }

        coordinatesRepository.delete(target);
        cityChanged.fire(new CityChanged());
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteHuman(Integer id, Integer replacementId)
            throws EntityNotFoundException, ReplacementRequiredException, SameObjectException {
        Human target = findHuman(id);
        List<City> linked = cityRepository.findByGovernor(id);

        if (!linked.isEmpty()) {
            checkReplacement(id, replacementId, "губернатора");
            Human replacement = findHuman(replacementId);
            linked.forEach(city -> city.setGovernor(replacement));
        }

        humanRepository.delete(target);
        cityChanged.fire(new CityChanged());
    }

    private void checkReplacement(Integer id, Integer replacementId, String what)
            throws ReplacementRequiredException, SameObjectException {
        if (replacementId == null) {
            throw new ReplacementRequiredException(what);
        }
        if (replacementId.equals(id)) {
            throw new SameObjectException("Нельзя заменить объект им же самим");
        }
    }
}
