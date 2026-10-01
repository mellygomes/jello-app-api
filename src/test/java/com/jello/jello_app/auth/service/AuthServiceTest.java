package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfirmationRepository confirmationRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        loginRequest = createLoginRequest();
        user = createUser();
    }

    // Teste de verificacao de login
    @Test
    void shouldLogin() {
        Authentication authentication = mock(Authentication.class);
        AppUserDetails userDetails = mock(AppUserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))
        ).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nick");
        when(userRepository.findByUsername("nick")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Authentication result = authService.login(loginRequest);

        assertNotNull(result);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).save(any());
    }

    // Teste de verificacao de login com credencial invalida
    @Test
    void shouldFailWhenInvalidLoginCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))
        ).thenThrow(new BadCredentialsException("Usuario ou senha inválidos"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Usuario ou senha inválidos", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any());
        verifyNoInteractions(userRepository);
    }

    // Teste de autenticacao de usuario
    @Test
    void shouldAuthenticateUser() {
        Authentication authentication = mock(Authentication.class);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(authentication.getName()).thenReturn("usuario123");
        when(userRepository.findByUsername("usuario123")).thenReturn(user);

        User result = authService.getAuthenticatedUser();

        assertEquals(user.getId(), result.getId());

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    // Teste de autenticacao de usuario falha
    @Test
    void shouldFailWhenUserIsNotAuthenticated() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        assertThrows(NullPointerException.class, () -> authService.getAuthenticatedUser());

        verifyNoInteractions(userRepository);
    }

    // Teste de chave de verificacao de conta
    @Test
    void shouldVerifyAccountKey() {
        String token = "tokenUltraSecreto";

        Confirmation confirmation = new Confirmation();
        confirmation.setId(1L);
        confirmation.setConfirmationKey(token);
        confirmation.setUser(user);

        when(confirmationRepository.findByConfirmationKey(token)).thenReturn(Optional.of(confirmation));
        when(userRepository.findByEmail(confirmation.getUser().getEmail())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.verifyAccountKey(token);

        verify(confirmationRepository, times(1)).findByConfirmationKey(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any());
        verify(confirmationRepository, times(1)).delete(any());
    }

    // Teste de chave de verificacao de conta com chave invalida
    @Test
    void shouldFailWhenVerifyAccountKey() {
        when(confirmationRepository.findByConfirmationKey(anyString()))
                .thenThrow(new RuntimeException("Chave de confirmação não encontrada!"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.verifyAccountKey("chaveInexistenteMesmo"));

        assertEquals("Chave de confirmação não encontrada!", exception.getMessage());

        verify(confirmationRepository, times(1)).findByConfirmationKey(anyString());
        verifyNoInteractions(userRepository);
        verify(confirmationRepository, times(0)).delete(any());
    }

    // Limpa o contexto apos os testes
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nick");
        request.setPassword("123");

        return request;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("nickname");
        user.setEmail("email@teste.com");

        return user;
    }

}