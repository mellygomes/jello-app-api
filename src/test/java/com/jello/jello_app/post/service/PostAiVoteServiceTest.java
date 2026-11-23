package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.common.exception.BusinessException;
import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.post.exception.PostNotFoundException;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.model.PostAiVote;
import com.jello.jello_app.post.repository.PostAiVoteRepository;
import com.jello.jello_app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostAiVoteServiceTest {

    @Mock
    private PostAiVoteRepository aiVoteRepository;

    @Mock
    private PostService postService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PostAiVoteService postAiVoteService;

    private User voter;
    private Post post;

    @BeforeEach
    void setUp() {
        voter = new User();
        voter.setId(1L);

        User author = new User();
        author.setId(2L);

        post = new Post();
        post.setId(10L);
        post.setUser(author);
    }

    // Primeiro voto do usuario cria o registro
    @Test
    void shouldCreateVoteWhenUserHasNotVotedYet() {
        when(authService.getAuthenticatedUser()).thenReturn(voter);
        when(postService.getVisiblePost(10L, voter)).thenReturn(post);
        when(aiVoteRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(aiVoteRepository.countByPostIdAndVoteAi(10L, true)).thenReturn(1L);
        when(aiVoteRepository.countByPostIdAndVoteAi(10L, false)).thenReturn(0L);

        AiVoteResponseDTO result = postAiVoteService.vote(10L, true);

        ArgumentCaptor<PostAiVote> captor = ArgumentCaptor.forClass(PostAiVote.class);
        verify(aiVoteRepository).save(captor.capture());

        assertTrue(captor.getValue().isVoteAi());
        assertEquals(voter, captor.getValue().getUser());
        assertEquals(1, result.getAi());
        assertEquals(0, result.getHuman());
        assertEquals(Boolean.TRUE, result.getMyVote());
    }

    // Votar de novo troca o voto em vez de criar outro
    @Test
    void shouldChangeExistingVote() {
        PostAiVote existing = new PostAiVote();
        existing.setPost(post);
        existing.setUser(voter);
        existing.setVoteAi(true);

        when(authService.getAuthenticatedUser()).thenReturn(voter);
        when(postService.getVisiblePost(10L, voter)).thenReturn(post);
        when(aiVoteRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        AiVoteResponseDTO result = postAiVoteService.vote(10L, false);

        assertFalse(existing.isVoteAi());
        assertEquals(Boolean.FALSE, result.getMyVote());
        verify(aiVoteRepository).save(existing);
    }

    // Autor nao pode votar na propria postagem
    @Test
    void shouldNotAllowAuthorToVoteOnOwnPost() {
        post.setUser(voter);

        when(authService.getAuthenticatedUser()).thenReturn(voter);
        when(postService.getVisiblePost(10L, voter)).thenReturn(post);

        BusinessException exception = assertThrows(BusinessException.class, () -> postAiVoteService.vote(10L, true));

        assertTrue(exception.getMessage().contains("própria postagem"));
        verify(aiVoteRepository, never()).save(any());
    }

    // Post escondido (banido) nao pode receber voto
    @Test
    void shouldFailWhenPostIsNotVisible() {
        when(authService.getAuthenticatedUser()).thenReturn(voter);
        when(postService.getVisiblePost(10L, voter)).thenThrow(new PostNotFoundException(10L));

        assertThrows(PostNotFoundException.class, () -> postAiVoteService.vote(10L, true));

        verify(aiVoteRepository, never()).save(any());
    }

    @Test
    void shouldRemoveVote() {
        when(authService.getAuthenticatedUser()).thenReturn(voter);
        when(postService.getVisiblePost(10L, voter)).thenReturn(post);

        AiVoteResponseDTO result = postAiVoteService.removeVote(10L);

        assertNull(result.getMyVote());
        verify(aiVoteRepository).deleteByUserIdAndPostId(1L, 10L);
    }
}
