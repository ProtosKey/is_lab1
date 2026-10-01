package dev.protoskey.repository;

import dev.protoskey.entity.Human;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HumanRepository {
    @PersistenceContext(unitName = "is_lab_1PU")
    private EntityManager em;

    public Optional<Human> findById(Integer id) {
        return Optional.ofNullable(em.find(Human.class, id));
    }

    public List<Human> findAll() {
        return em.createQuery("select h from Human h order by h.id", Human.class).getResultList();
    }

    public Human save(Human human) {
        em.persist(human);
        return human;
    }

    public Human update(Human human) {
        return em.merge(human);
    }

    public void delete(Human human) {
        em.remove(em.contains(human) ? human : em.merge(human));
    }
}
