package dev.protoskey.service;

import dev.protoskey.entity.User;
import dev.protoskey.exception.AuthenticationException;
import dev.protoskey.exception.LoginAlreadyTakenException;
import dev.protoskey.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class AuthService {
    @Inject
    private UserRepository userRepository;

    @Transactional(rollbackOn = Exception.class)
    public User register(String login, String password) throws LoginAlreadyTakenException {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new LoginAlreadyTakenException(login);
        }

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(hash(password));
        return userRepository.save(user);
    }

    public User login(String login, String password) throws AuthenticationException {
        return userRepository.findByLogin(login)
                .filter(user -> user.getPasswordHash().equals(hash(password)))
                .orElseThrow(AuthenticationException::new);
    }

    private String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }
}
