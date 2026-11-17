package com.jello.jello_app.report.repository;

import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.common.model.AuditorAwareImpl;
import com.jello.jello_app.report.model.PostReport;
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
class PostReportRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private PostReportRepository postReportRepository;

    @Autowired
    private EntityManager entityManager;

    private User reporter;
    private Post post;
    private Moderator moderator;

    @BeforeEach
    void createRequiredRelations() {
        String suffix = UUID.randomUUID().toString();

        reporter = createUser("reporter-" + suffix, "reporter-" + suffix + "@test.invalid");
        entityManager.persist(reporter);

        User moderatorUser = createUser("moderator-" + suffix, "moderator-" + suffix + "@test.invalid");
        entityManager.persist(moderatorUser);

        moderator = new Moderator();
        moderator.setUser(moderatorUser);
        entityManager.persist(moderator);

        post = new Post();
        post.setUser(reporter);
        post.setTitle("Post de integração");
        post.setContent("Conteúdo do post de teste");
        entityManager.persist(post);

        entityManager.flush();
    }

    @Test
    void shouldInsertReport() {
        PostReport saved = postReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        assertNotNull(reportId);
        assertTrue(postReportRepository.findById(reportId).isPresent());
    }

    @Test
    void shouldReadReport() {
        PostReport saved = postReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        PostReport found = postReportRepository.findById(reportId).orElseThrow();

        assertEquals(reportId, found.getId());
        assertEquals(post.getId(), found.getReportedPost().getId());
        assertEquals(reporter.getId(), found.getReporterUser().getId());
        assertEquals(moderator.getId(), found.getModeratorAnalyst().getId());
        assertFalse(found.isApproved());
    }

    @Test
    void shouldUpdateReport() {
        PostReport saved = postReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();
        entityManager.clear();

        PostReport found = postReportRepository.findById(reportId).orElseThrow();
        found.setApproved(true);
        postReportRepository.flush();
        entityManager.clear();

        PostReport updated = postReportRepository.findById(reportId).orElseThrow();
        assertTrue(updated.isApproved());
    }

    @Test
    void shouldDeleteReport() {
        PostReport saved = postReportRepository.saveAndFlush(createReport());
        Long reportId = saved.getId();

        postReportRepository.deleteById(reportId);
        postReportRepository.flush();
        entityManager.clear();

        assertFalse(postReportRepository.existsById(reportId));
    }

    private PostReport createReport() {
        PostReport report = new PostReport();
        report.setReportedPost(post);
        report.setReporterUser(reporter);
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
