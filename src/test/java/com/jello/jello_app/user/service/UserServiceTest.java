package com.jello.jello_app.user.service;

import com.jello.jello_app.auth.dto.RegisterRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.event.UserEvent;
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

        assertEquals("Tipo de usuario não encontrado! (ROLE_USER)", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(confirmationRepository, never()).save(any(Confirmation.class));
        verify(applicationEventPublisher, never()).publishEvent(any(UserEvent.class));
    }

    // Criacao de usuario falha quando tenta criar um usuario com dados ja existentes
    @Test
    void shouldThrowExceptionWhenDataIntegrityFails() {
        Role role = createRole();
        RegisterRequest registerRequest = createRegisterRequest();

        when(roleRepository.findByName(RoleType.ROLE_USER.getName())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("Cripto");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Email já cadastrado"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(registerRequest));

        assertEquals("Email já cadastrado", exception.getMessage());
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
    }

    // Get do usuario quando nao existe
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(id));

        assertEquals("Usuário não encontrado!", exception.getMessage());
        verify(userRepository, times(1)).findById(id);
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

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("senhaCriptografadaMesmo");
        when(userRepository.save(user)).thenReturn(user);

        ProfileDTO userUpdated = userService.updateUser(updateRequest, id);

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(user);

        assertEquals("super mega nome", userUpdated.getFirstName());
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