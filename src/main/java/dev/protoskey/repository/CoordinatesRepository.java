package dev.protoskey.repository;

import dev.protoskey.entity.Coordinates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoordinatesRepository {
    @PersistenceContext(unitName = "is_lab_1PU")
    private EntityManager em;

    public Optional<Coordinates> findById(Integer id) {
        return Optional.ofNullable(em.find(Coordinates.class, id));
    }

    public List<Coordinates> findAll() {
        return em.createQuery("select c from Coordinates c order by c.id", Coordinates.class)
                .getResultList();
    }

    public Coordinates save(Coordinates coordinates) {
        em.persist(coordinates);
        return coordinates;
    }

    public Coordinates update(Coordinates coordinates) {
        return em.merge(coordinates);
    }

    public void delete(Coordinates coordinates) {
        em.remove(em.contains(coordinates) ? coordinates : em.merge(coordinates));
    }
}
