package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.confirmation.exception.ConfirmationNotFoundException;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.service.ConfirmationService;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import com.jello.jello_app.user.service.UserService;
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
    private UserService userService;

    @Mock
    private ConfirmationService confirmationService;

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

        AppUserDetails userDetails = new AppUserDetails();
        userDetails.setId(1L);
        userDetails.setUsername("nick");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))
        ).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Authentication result = authService.login(loginRequest);

        assertNotNull(result);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).getUserById(anyLong());
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
    void shouldNotReturnUserWhenIsNotAuthenticated() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        User user = authService.getAuthenticatedUser();

        assertNull(user);
        verify(userRepository).findByUsername(anyString());
    }

    // Teste de chave de verificacao de conta
    @Test
    void shouldVerifyAccountKey() {
        String token = "tokenUltraSecreto";

        Confirmation confirmation = new Confirmation();
        confirmation.setId(1L);
        confirmation.setConfirmationKey(token);
        confirmation.setUser(user);

        when(confirmationService.getConfirmationByKey(token)).thenReturn(confirmation);
        when(userService.getUserById(confirmation.getUser().getId())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.verifyAccountKey(token);

        verify(confirmationService, times(1)).getConfirmationByKey(anyString());
        verify(userService, times(1)).getUserById(anyLong());
        verify(userRepository, times(1)).save(any());
        verify(confirmationService, times(1)).deleteConfirmation(any());
    }

    // Teste de chave de verificacao de conta com chave invalida
    @Test
    void shouldFailWhenVerifyAccountKey() {
        when(confirmationService.getConfirmationByKey("chaveInexistenteMesmo"))
                .thenThrow(new ConfirmationNotFoundException("chaveInexistenteMesmo"));

        ConfirmationNotFoundException exception = assertThrows(ConfirmationNotFoundException.class,
                () -> authService.verifyAccountKey("chaveInexistenteMesmo"));

        assertEquals("Chave de confirmação não encontrada: chaveInexistenteMesmo", exception.getMessage());

        verify(confirmationService, times(1)).getConfirmationByKey(anyString());
        verifyNoInteractions(userService);
        verifyNoInteractions(userRepository);
        verify(confirmationService, times(0)).deleteConfirmation(any());
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