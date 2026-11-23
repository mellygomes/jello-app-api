package com.jello.jello_app.post.repository;

import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.common.model.AuditorAwareImpl;
import com.jello.jello_app.enumeration.PostSort;
import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
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

    // Ordenacao do explorar/feed: recente, antigo, mais curtido e mais comentado (com desempate estavel)
    @Test
    void shouldSortPostsByRecentOldestPopularAndCommented() {
        User fan1 = persistUser("fan1");
        User fan2 = persistUser("fan2");

        Post a = postRepository.saveAndFlush(createPost("A"));
        Post b = postRepository.saveAndFlush(createPost("B"));
        Post c = postRepository.saveAndFlush(createPost("C"));

        // A: 2 curtidas, 0 comentarios | B: 0 curtidas, 2 comentarios | C: 1 curtida, 1 comentario
        a.getLikes().add(fan1);
        a.getLikes().add(fan2);
        c.getLikes().add(fan1);
        persistComment(b, fan1);
        persistComment(b, fan2);
        persistComment(c, fan2);
        entityManager.flush();
        entityManager.clear();

        assertEquals(List.of("C", "B", "A"), titles(PostSort.RECENT, 0, 10));
        assertEquals(List.of("A", "B", "C"), titles(PostSort.OLDEST, 0, 10));
        assertEquals(List.of("A", "C", "B"), titles(PostSort.POPULAR, 0, 10));
        assertEquals(List.of("B", "C", "A"), titles(PostSort.COMMENTED, 0, 10));

        // paginacao nao repete post entre as paginas
        List<String> pagina0 = titles(PostSort.POPULAR, 0, 2);
        List<String> pagina1 = titles(PostSort.POPULAR, 1, 2);
        assertEquals(List.of("A", "C"), pagina0);
        assertEquals(List.of("B"), pagina1);
    }

    // Filtro por varias tags nao pode duplicar o post (agora e subquery, sem DISTINCT)
    @Test
    void shouldFilterByTagsWithoutDuplicatingPosts() {
        Tag t1 = persistTag("t1");
        Tag t2 = persistTag("t2");

        Post tagged = createPost("com-tags");
        tagged.getTags().add(t1);
        tagged.getTags().add(t2);
        postRepository.saveAndFlush(tagged);
        postRepository.saveAndFlush(createPost("sem-tag"));
        entityManager.clear();

        FilterRequestDTO filter = new FilterRequestDTO();
        filter.setTags(List.of(t1.getId(), t2.getId()));

        Page<Post> result = postRepository.findAll(
                Specification.where(PostSpecification.withFilter(filter)).and(PostSpecification.sortedBy(PostSort.POPULAR)),
                PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("com-tags", result.getContent().getFirst().getTitle());
    }

    // V7: excluir quem so mexeu num post (ex: moderador que classificou) nao pode apagar nem travar o post
    @Test
    void shouldKeepPostWhenUserWhoEditedItIsDeleted() {
        User editor = persistUser("editor");
        Post saved = postRepository.saveAndFlush(createPost("editado"));
        Long postId = saved.getId();

        entityManager.createNativeQuery("update tb_post set updated_by = :editor where id = :id")
                .setParameter("editor", editor.getId())
                .setParameter("id", postId)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        entityManager.remove(entityManager.find(User.class, editor.getId()));
        entityManager.flush();
        entityManager.clear();

        Post kept = postRepository.findById(postId).orElseThrow();
        assertNull(kept.getUpdatedBy());
    }

    private List<String> titles(PostSort sort, int page, int size) {
        Specification<Post> spec = Specification
                .where(PostSpecification.byUserId(author.getId()))
                .and(PostSpecification.sortedBy(sort));

        return postRepository.findAll(spec, PageRequest.of(page, size)).getContent().stream()
                .map(Post::getTitle)
                .toList();
    }

    private User persistUser(String name) {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setFirstName(name);
        user.setUsername(name + "-" + suffix);
        user.setEmail(name + "-" + suffix + "@test.invalid");
        user.setPassword("senha-de-teste");
        user.setEnabled(true);
        entityManager.persist(user);
        return user;
    }

    private Tag persistTag(String name) {
        Tag tag = new Tag();
        tag.setName(name + "-" + UUID.randomUUID());
        tag.setColor("#000000");
        entityManager.persist(tag);
        return tag;
    }

    private void persistComment(Post post, User user) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent("comentario");
        entityManager.persist(comment);
    }

    private Post createPost(String title) {
        Post post = createPost();
        post.setTitle(title);
        return post;
    }

    private Post createPost() {
        Post post = new Post();
        post.setUser(author);
        post.setTitle("Post de integração");
        post.setContent("Conteúdo do post de teste");
        return post;
    }
}
