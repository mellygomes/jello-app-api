package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.enumeration.PostClassification;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostModerationServiceTest {

    @Mock
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PostResponseAssembler postResponseAssembler;

    @InjectMocks
    private PostModerationService postModerationService;

    private User moderatorUser;
    private Moderator moderator;
    private Post post;

    @BeforeEach
    void setUp() {
        moderatorUser = new User();
        moderatorUser.setId(1L);

        moderator = new Moderator();
        moderator.setId(5L);
        moderator.setUser(moderatorUser);

        post = new Post();
        post.setId(10L);
    }

    @Test
    void shouldClassifyPostAsAi() {
        when(authService.getAuthenticatedUser()).thenReturn(moderatorUser);
        when(postService.getPostById(10L)).thenReturn(post);
        when(userRoleService.getModeratorByUser(moderatorUser)).thenReturn(moderator);
        when(postRepository.save(post)).thenReturn(post);
        when(postResponseAssembler.toResponse(post, moderatorUser)).thenReturn(PostResponseDTO.builder().id(10L).classification("AI").build());

        PostResponseDTO result = postModerationService.classifyPost(10L, PostClassification.AI);

        assertTrue(post.isAiClassified());
        assertFalse(post.isHumanClassified());
        assertEquals(moderator, post.getModerator());
        assertEquals("AI", result.getClassification());
    }

    @Test
    void shouldClassifyPostAsHuman() {
        post.setAiClassified(true);

        when(authService.getAuthenticatedUser()).thenReturn(moderatorUser);
        when(postService.getPostById(10L)).thenReturn(post);
        when(userRoleService.getModeratorByUser(moderatorUser)).thenReturn(moderator);
        when(postRepository.save(post)).thenReturn(post);

        postModerationService.classifyPost(10L, PostClassification.HUMAN);

        assertFalse(post.isAiClassified());
        assertTrue(post.isHumanClassified());
    }

    // NONE tira a classificacao e o moderador responsavel
    @Test
    void shouldClearClassification() {
        post.setAiClassified(true);
        post.setModerator(moderator);

        when(authService.getAuthenticatedUser()).thenReturn(moderatorUser);
        when(postService.getPostById(10L)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(post);

        postModerationService.classifyPost(10L, PostClassification.NONE);

        assertFalse(post.isAiClassified());
        assertFalse(post.isHumanClassified());
        assertNull(post.getModerator());
        verifyNoInteractions(userRoleService);
    }

    @Test
    void shouldBanAndUnbanPost() {
        when(postService.getPostById(10L)).thenReturn(post);

        postModerationService.banPost(10L);
        assertTrue(post.isBanned());

        postModerationService.unbanPost(10L);
        assertFalse(post.isBanned());

        verify(postRepository, times(2)).save(any(Post.class));
    }
}
