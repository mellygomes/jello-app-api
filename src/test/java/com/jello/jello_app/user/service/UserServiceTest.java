package com.jello.jello_app.user.service;

import com.jello.jello_app.auth.dto.RegisterRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.event.UserEvent;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.service.UserAvatarService;
import com.jello.jello_app.image.service.UserCoverService;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.repository.RoleRepository;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UpdateUserRequest;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConfirmationRepository confirmationRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserCoverService coverService;

    @Mock
    private UserAvatarService avatarService;

    @Mock
    private PostService postService;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    // Criacao de usuario mesmo com tudo valido
    @Test
    void shouldRegisterUser() {
        Role role = createRole();
        RegisterRequest registerRequest = createRegisterRequest();

        when(roleRepository.findByName(RoleType.ROLE_USER.getName())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("criptografada");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userSaved = invocation.getArgument(0);
            userSaved.setId(1L);
            return userSaved;
        });

        when(confirmationRepository.save(any(Confirmation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.register(registerRequest);

        assertNotNull(savedUser);
        assertEquals("superEmail@mail.com", savedUser.getEmail());
        assertEquals("nickname", savedUser.getUsername());

        verify(userRepository, times(1)).save(any(User.class));
        verify(confirmationRepository, times(1)).save(any(Confirmation.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(UserEvent.class));
    }

    // Criacao falha de usuario quando role nao encontrado
    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        RegisterRequest request = createRegisterRequest();
        when(roleRepository.findByName(any(String.class))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(confirmationRepository, never()).save(any(Confirmation.class));
        verify(applicationEventPublisher, never()).publishEvent(any(UserEvent.class));

        assertEquals("Tipo de usuario não encontrado! (ROLE_USER)", exception.getMessage());
    }

    // Criacao de usuario falha quando tenta criar um usuario com dados ja existentes
    @Test
    void shouldThrowExceptionWhenDataIntegrityFails() {
        Role role = createRole();
        RegisterRequest registerRequest = createRegisterRequest();

        UserCover cover = new UserCover();
        cover.setFileName("Capa de teste");

        UserAvatar avatar = new UserAvatar();
        avatar.setFileName("Super avatar teste");

        when(roleRepository.findByName(RoleType.ROLE_USER.getName())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("Cripto");
        when(coverService.createUserCover()).thenReturn(cover);
        when(avatarService.createUserAvatar()).thenReturn(avatar);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Email já cadastrado"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(registerRequest));

        assertEquals("Email já cadastrado", exception.getMessage());

        verify(roleRepository, times(1)).findByName(any());
        verify(passwordEncoder, times(1)).encode(any());
        verify(coverService, times(1)).createUserCover();
        verify(avatarService, times(1)).createUserAvatar();
        verify(userRepository, times(1)).save(any());
        verify(confirmationRepository, never()).save(any());
    }

    // Get do usuário quando existe
    @Test
    void shouldGetUserById() {
        Long id = 1L;

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertEquals(id, result.getId());

        verify(roleRepository, times(1)).findById(anyLong());
    }

    // Get do usuario quando nao existe
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(id));

        assertEquals("Usuário não encontrado!", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
    }

    // Get do profile do usuario
    @Test
    void shouldGetUserProfile() {
        Long id = 1L;
        long posts = 2;
        long followers = 4;
        long followings = 23;
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(postService.getCountPostsByUserId(id)).thenReturn(posts);
        when(followRepository.countByFollowerId(id)).thenReturn(followers);
        when(followRepository.countByFollowingId(id)).thenReturn(followings);

        ProfileDTO profile = userService.getUserProfile(id);

        assertEquals(followings, profile.getFollowers());
        assertEquals(followers, profile.getFollowing());
        assertEquals(posts, profile.getPosts());

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(postService, times(1)).getCountPostsByUserId(any(Long.class));
        verify(followRepository, times(1)).countByFollowingId(any(Long.class));
        verify(followRepository, times(1)).countByFollowerId(any(Long.class));
    }

    // Get do profile do usuario quando usuario nao existe
    @Test
    void shouldThrowExceptionWhenUserProfileNotFound() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserProfile(id));

        assertEquals("Usuário não encontrado!", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
        verify(postService, never()).getCountPostsByUserId(anyLong());
    }

    // Delete de usuário quando existe
    @Test
    void shouldDeleteUserWhenUserExists() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));

        userService.deleteUser(id);

        verify(userRepository).delete(any(User.class));
    }

    // Delete de usuário quando nao existe
    @Test
    void shouldFailDeletionWhenUserDoesNotExist() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(id));

        assertEquals("Usuário não encontrado!", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    // Atualizacao de usuario quando existe
    @Test
    void shouldUpdateUser() {
        UpdateUserRequest updateRequest = createUpdateRequest();

        Long id = 1L;
        long posts = 2;
        long followers = 3;
        long followings = 4;

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(postService.getCountPostsByUserId(1L)).thenReturn(posts);
        when(followRepository.countByFollowerId(id)).thenReturn(followings);
        when(followRepository.countByFollowingId(id)).thenReturn(followers);
        when(userRepository.save(user)).thenReturn(user);

        ProfileDTO userUpdated = userService.updateUser(updateRequest, id);

        assertEquals("super mega nome", userUpdated.getFirstName());

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
        verify(postService, times(1)).getCountPostsByUserId(anyLong());
        verify(followRepository, times(1)).countByFollowerId(anyLong());
        verify(followRepository, times(1)).countByFollowingId(anyLong());
    }

    // Atualizacao de usuario quando nao existe
    @Test
    void shouldThrowExceptionWhenUpdateInvalidUser() {
        UpdateUserRequest request = new UpdateUserRequest();
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(request, id));

        assertEquals("Usuário não encontrado!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    private Role createRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleType.ROLE_USER.getName());
        role.setUsers(List.of());

        return role;
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("superEmail@mail.com");
        registerRequest.setFirstName("nome insano");
        registerRequest.setLastName("sobrenome foda");
        registerRequest.setPassword("senhaMesmo");
        registerRequest.setUsername("nickname");

        return registerRequest;
    }

    private UpdateUserRequest createUpdateRequest() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("super mega nome");
        updateRequest.setLastName("super sobrenome");
        updateRequest.setBio("biografia insana");
        updateRequest.setPassword("novaSenhaMesmo");

        return updateRequest;
    }

}