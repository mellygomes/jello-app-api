package com.jello.jello_app.follow.service;

import com.jello.jello_app.auth.service.AuthServiceImpl;
import com.jello.jello_app.follow.model.Follow;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private AuthServiceImpl authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        follower = createUser(1L, "usuarioUm@mail.com");
        following = createUser(2L, "usuarioDois@mail.com");
    }

    // Teste para seguir um usuario
    @Test
    void shouldFollowUser() {
        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(2L)).thenReturn(following);
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);

        followService.followUser(2L);

        verify(authService, times(1)).getAuthenticatedUser();
        verify(userService, times(1)).getUserById(anyLong());
        verify(followRepository, times(1)).existsByFollowerAndFollowing(any(), any());
        verify(followRepository, times(1)).save(any());
    }

    // Teste para tentar seguir si mesmo
    @Test
    void shouldThrowExceptionWhenFollowingYourself() {
        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(1L)).thenReturn(follower);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> followService.followUser(follower.getId()));

        assertTrue(exception.getMessage().contains("Você não pode seguir a si mesmo!"));

        verify(followRepository, never()).save(any());
    }

    // Teste para tentar seguir um usuario que ja é seguido
    @Test
    void shouldThrowExceptionWhenFollowingAlreadyFollowedUser() {
        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(2L)).thenReturn(following);
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> followService.followUser(2L));

        assertTrue(exception.getMessage().contains("Você já segue esse usuário!"));

        verify(followRepository, never()).save(any());
    }

    // Teste para deixar de seguir um usuario
    @Test
    void shouldUnfollowUser() {
        Follow follow = new Follow();
        follow.setId(3L);
        follow.setFollower(follower);
        follow.setFollowing(following);

        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(2L)).thenReturn(following);
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(follow));

        followService.unfollowUser(following.getId());

        verify(followRepository).delete(any());
    }

    // Teste para tentar deixar de seguir si mesmo
    @Test
    void shouldThrowExceptionWhenUnfollowingYourself() {
        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(1L)).thenReturn(follower);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> followService.unfollowUser(follower.getId()));

        assertTrue(exception.getMessage().contains("Você não pode deixar de seguir a si mesmo."));

        verify(followRepository, never()).save(any());
    }

    // Teste para tentar deixar de seguir um usuário que já nao segue
    @Test
    void shouldThrowExceptionWhenUnfollowingAlreadyUnfollowedUser() {
        when(authService.getAuthenticatedUser()).thenReturn(follower);
        when(userService.getUserById(2L)).thenReturn(following);
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> followService.unfollowUser(following.getId()));

        assertTrue(exception.getMessage().contains("Você não segue esse usuário"));

        verify(followRepository, never()).save(any());
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);

        return user;
    }
}