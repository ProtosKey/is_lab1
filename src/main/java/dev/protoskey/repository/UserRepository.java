package dev.protoskey.repository;

import dev.protoskey.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {
    @PersistenceContext(unitName = "is_lab_1PU")
    private EntityManager em;

    public Optional<User> findByLogin(String login) {
        return em.createQuery("select u from User u where u.login = :login", User.class)
                .setParameter("login", login)
                .getResultList()
                .stream()
                .findFirst();
    }

    public User save(User user) {
        em.persist(user);
        return user;
    }
}
