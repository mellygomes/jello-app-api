package com.jello.jello_app.report.repository;

import com.jello.jello_app.common.model.AuditorAwareImpl;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
class UserReportRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private EntityManager entityManager;

    private User reportedUser;
    private User reporterUser;
    private Moderator moderator;

    @BeforeEach
    void createRequiredRelations() {
        String suffix = UUID.randomUUID().toString();

        reportedUser = createUser("reported-" + suffix, "reported-" + suffix + "@test.invalid");
        reporterUser = createUser("reporter-" + suffix, "reporter-" + suffix + "@test.invalid");
        User moderatorUser = createUser("moderator-" + suffix, "moderator-" + suffix + "@test.invalid");

        entityManager.persist(reportedUser);
        entityManager.persist(reporterUser);
        entityManager.persist(moderatorUser);

        moderator = new Moderator();
        moderator.setUser(moderatorUser);
        entityManager.persist(moderator);
        entityManager.flush();
    }

    @Test
    void shouldInsertUserReport() {
        UserReport saved = userReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        assertNotNull(reportId);
        assertTrue(userReportRepository.findById(reportId).isPresent());
    }

    @Test
    void shouldReadUserReport() {
        UserReport saved = userReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        UserReport found = userReportRepository.findById(reportId).orElseThrow();

        assertEquals(reportId, found.getId());
        assertEquals(reportedUser.getId(), found.getReportedUser().getId());
        assertEquals(reporterUser.getId(), found.getReporterUser().getId());
        assertEquals(moderator.getId(), found.getModeratorAnalyst().getId());
        assertFalse(found.isApproved());
    }

    @Test
    void shouldUpdateUserReport() {
        UserReport saved = userReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        UserReport found = userReportRepository.findById(reportId).orElseThrow();
        found.setApproved(true);
        userReportRepository.flush();
        entityManager.clear();

        UserReport updated = userReportRepository.findById(reportId).orElseThrow();
        assertTrue(updated.isApproved());
    }

    @Test
    void shouldDeleteUserReport() {
        UserReport saved = userReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();

        userReportRepository.deleteById(reportId);
        userReportRepository.flush();
        entityManager.clear();

        assertFalse(userReportRepository.existsById(reportId));
    }

    private UserReport createReport() {
        UserReport report = new UserReport();
        report.setReportedUser(reportedUser);
        report.setReporterUser(reporterUser);
        report.setModeratorAnalyst(moderator);
        return report;
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setFirstName("Teste integração");
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("senha-de-teste");
        user.setEnabled(true);
        user.setBanned(false);
        return user;
    }
}
