package com.jello.jello_app.comment.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.repository.CommentRepository;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = createUser();
        post = createPost();
        comment = createComment(3L, "Super mega comentário mesmo!", user, post);
    }

    // Teste para adicionar um comentário, fluxo normal
    @Test
    void shouldAddComment() {
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(postService.getPostById(post.getId())).thenReturn(post);

        commentService.addComment(comment.getContent(), post.getId());

        assertTrue(comment.getContent().contains("Super mega comentário mesmo!"));
        assertEquals(post.getTitle(), comment.getPost().getTitle());
        assertEquals(user.getUsername(), comment.getUser().getUsername());

        verify(authService, times(1)).getAuthenticatedUser();
        verify(postService, times(1)).getPostById(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // Teste para simular um erro ao tentar salvar um comentario
    @Test
    void shouldThrowExceptionWhenCommentSaveFails() {
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(postService.getPostById(post.getId())).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenThrow(new RuntimeException("Erro ao salvar o comentário"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> commentService.addComment(comment.getContent(), post.getId())
        );

        assertEquals("Erro ao salvar o comentário", exception.getMessage());

        verify(authService, times(1)).getAuthenticatedUser();
        verify(postService, times(1)).getPostById(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // Teste para pegar todos os comentarios de um post pelo seu ID
    @Test
    void shouldGetAllCommentsFromPost() {
        Comment comment1 = createComment(4L, "Comentario 1 mesmo", user, post);
        Comment comment2 = createComment(5L, "Comentario 2 mesmo", user, post);

        when(postService.getPostById(post.getId())).thenReturn(post);
        when(commentRepository.findByPost(post)).thenReturn(List.of(comment1, comment2));

        List<CommentResponseDTO> result = commentService.getAllCommentsFromPost(post.getId());

        assertEquals(2, result.size());

        verify(commentRepository, times(1)).findByPost(any(Post.class));
    }

    // Teste de deleção de comentario com fluxo normal
    @Test
    void shouldDeleteComment() {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(comment.getId());

        verify(commentRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).delete(any());
    }

    // Teste de deleçao de comentario quando gera erro ao recuperar o comentario
    @Test
    void shouldThrowExceptionWhenDeletingCommentFails() {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> commentService.deleteComment(comment.getId())
        );

        assertEquals("Comentário não encontrado!", exception.getMessage());

        verify(commentRepository, times(1)).findById(anyLong());
        verify(commentRepository, never()).delete(any());
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Usuario 1");

        return user;
    }

    private Post createPost() {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("Post 2");

        return post;
    }

    private Comment createComment(Long id, String content, User user, Post post) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);

        return comment;
    }
}