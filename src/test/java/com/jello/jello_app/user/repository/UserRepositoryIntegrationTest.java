package com.jello.jello_app.user.repository;

import com.jello.jello_app.common.model.AuditorAwareImpl;
import com.jello.jello_app.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AuditorAwareImpl.class)
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertUser() {
        User saved = userRepository.saveAndFlush(createUser());
        Long userId = saved.getId();
        entityManager.clear();

        assertNotNull(userId);
        assertTrue(userRepository.findById(userId).isPresent());
    }

    @Test
    void shouldReadUserAndFindByUniqueFields() {
        User saved = userRepository.saveAndFlush(createUser());
        Long userId = saved.getId();
        String username = saved.getUsername();
        String email = saved.getEmail();
        entityManager.clear();

        User found = userRepository.findById(userId).orElseThrow();

        assertEquals(username, found.getUsername());
        assertEquals(email, found.getEmail());
        assertEquals(userId, userRepository.findByUsername(username).getId());
        assertEquals(userId, userRepository.findByEmail(email).getId());
        assertTrue(userRepository.existsByUsername(username));
        assertTrue(userRepository.existsByEmail(email));
    }

    @Test
    void shouldUpdateUser() {
        User saved = userRepository.saveAndFlush(createUser());
        Long userId = saved.getId();
        entityManager.clear();

        User user = userRepository.findById(userId).orElseThrow();
        user.setFirstName("Nome atualizado");
        userRepository.flush();
        entityManager.clear();

        assertEquals("Nome atualizado", userRepository.findById(userId).orElseThrow().getFirstName());
    }

    @Test
    void shouldDeleteUser() {
        User saved = userRepository.saveAndFlush(createUser());
        Long userId = saved.getId();

        userRepository.deleteById(userId);
        userRepository.flush();
        entityManager.clear();

        assertFalse(userRepository.existsById(userId));
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setFirstName("Usuário de integração");
        user.setUsername("user-" + suffix);
        user.setEmail("user-" + suffix + "@test.invalid");
        user.setPassword("senha-de-teste");
        user.setEnabled(true);
        user.setBanned(false);
        return user;
    }
}
