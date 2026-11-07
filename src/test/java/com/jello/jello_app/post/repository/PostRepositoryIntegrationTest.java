package com.jello.jello_app.post.repository;

import com.jello.jello_app.common.model.AuditorAwareImpl;
import com.jello.jello_app.post.model.Post;
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
class PostRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    private User author;

    @BeforeEach
    void createAuthor() {
        String suffix = UUID.randomUUID().toString();
        author = new User();
        author.setFirstName("Autor de integração");
        author.setUsername("author-" + suffix);
        author.setEmail("author-" + suffix + "@test.invalid");
        author.setPassword("senha-de-teste");
        author.setEnabled(true);
        author.setBanned(false);
        entityManager.persist(author);
        entityManager.flush();
    }

    @Test
    void shouldInsertPost() {
        Post saved = postRepository.saveAndFlush(createPost());
        Long postId = saved.getId();
        entityManager.clear();

        assertNotNull(postId);
        assertTrue(postRepository.findById(postId).isPresent());
    }

    @Test
    void shouldReadPostAndCountByAuthor() {
        Post saved = postRepository.saveAndFlush(createPost());
        Long postId = saved.getId();
        Long authorId = author.getId();
        entityManager.clear();

        Post found = postRepository.findById(postId).orElseThrow();

        assertEquals("Post de integração", found.getTitle());
        assertEquals(authorId, found.getUser().getId());
        assertEquals(1L, postRepository.countByUserId(authorId));
    }

    @Test
    void shouldUpdatePost() {
        Post saved = postRepository.saveAndFlush(createPost());
        Long postId = saved.getId();
        entityManager.clear();

        Post post = postRepository.findById(postId).orElseThrow();
        post.setTitle("Título atualizado");
        post.setContent("Conteúdo atualizado");
        postRepository.flush();
        entityManager.clear();

        Post updated = postRepository.findById(postId).orElseThrow();
        assertEquals("Título atualizado", updated.getTitle());
        assertEquals("Conteúdo atualizado", updated.getContent());
    }

    @Test
    void shouldDeletePost() {
        Post saved = postRepository.saveAndFlush(createPost());
        Long postId = saved.getId();

        postRepository.deleteById(postId);
        postRepository.flush();
        entityManager.clear();

        assertFalse(postRepository.existsById(postId));
    }

    private Post createPost() {
        Post post = new Post();
        post.setUser(author);
        post.setTitle("Post de integração");
        post.setContent("Conteúdo do post de teste");
        return post;
    }
}
