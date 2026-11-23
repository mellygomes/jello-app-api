package com.jello.jello_app.user.service;

import com.jello.jello_app.common.exception.BusinessException;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.user.dto.ChangePasswordRequestDTO;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import com.jello.jello_app.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Banir usuario, ver perfil de banido e trocar senha
@ExtendWith(MockitoExtension.class)
class UserModerationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    private User moderator;
    private User commonUser;

    @BeforeEach
    void setUp() {
        moderator = createUser(1L, RoleType.ROLE_MODERATOR.getName());
        commonUser = createUser(2L, RoleType.ROLE_USER.getName());
    }

    @Test
    void shouldBanCommonUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));

        userService.banUser(2L, 1L);

        assertTrue(commonUser.isBanned());
        verify(userRepository).save(commonUser);
    }

    @Test
    void shouldNotBanYourself() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(moderator));

        assertThrows(BusinessException.class, () -> userService.banUser(1L, 1L));

        assertFalse(moderator.isBanned());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotBanAnotherModerator() {
        User other = createUser(3L, RoleType.ROLE_MODERATOR.getName());
        when(userRepository.findById(3L)).thenReturn(Optional.of(other));

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.banUser(3L, 1L));

        assertTrue(exception.getMessage().contains("Moderadores"));
        assertFalse(other.isBanned());
    }

    @Test
    void shouldUnbanUser() {
        commonUser.setBanned(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));

        userService.unbanUser(2L);

        assertFalse(commonUser.isBanned());
        verify(userRepository).save(commonUser);
    }

    // Perfil de banido e escondido de usuario comum, mas moderador enxerga
    @Test
    void shouldHideBannedProfileFromCommonUser() {
        User viewer = createUser(4L, RoleType.ROLE_USER.getName());
        commonUser.setBanned(true);

        when(userRepository.findById(4L)).thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));

        assertThrows(UserNotFoundException.class, () -> userService.getUserProfile(2L, 4L));
    }

    @Test
    void shouldShowBannedProfileToModeratorWithEmail() {
        commonUser.setBanned(true);
        commonUser.setEmail("fulano@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));

        ProfileDTO profile = userService.getUserProfile(2L, 1L);

        assertTrue(profile.isBanned());
        assertEquals("fulano@mail.com", profile.getEmail());
    }

    @Test
    void shouldHideEmailFromOtherCommonUsers() {
        User viewer = createUser(4L, RoleType.ROLE_USER.getName());
        commonUser.setEmail("fulano@mail.com");

        when(userRepository.findById(4L)).thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));

        ProfileDTO profile = userService.getUserProfile(2L, 4L);

        assertNull(profile.getEmail());
        assertFalse(profile.isFollowedByMe());
    }

    @Test
    void shouldFailChangePasswordWhenCurrentIsWrong() {
        commonUser.setPassword("hash-atual");
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));
        when(passwordEncoder.matches("errada", "hash-atual")).thenReturn(false);

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("errada");
        request.setNewPassword("novasenha123");

        assertThrows(BusinessException.class, () -> userService.changePassword(2L, request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldChangePassword() {
        commonUser.setPassword("hash-atual");
        when(userRepository.findById(2L)).thenReturn(Optional.of(commonUser));
        when(passwordEncoder.matches("certa", "hash-atual")).thenReturn(true);
        when(passwordEncoder.encode("novasenha123")).thenReturn("hash-novo");

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("certa");
        request.setNewPassword("novasenha123");

        userService.changePassword(2L, request);

        assertEquals("hash-novo", commonUser.getPassword());
        verify(userRepository).save(commonUser);
    }

    private User createUser(Long id, String roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRoles(new HashSet<>(Set.of(role)));
        return user;
    }
}
