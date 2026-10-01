package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ConfirmationRepository confirmationRepository;

    public Authentication login(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        updateLogin(userDetails.getUsername());

        return authentication;
    }

    public void updateLogin(String username) {
        User user = userRepository.findByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    public void verifyAccountKey(String token) {
        Confirmation confirmation = confirmationRepository.findByConfirmationKey(token)
                .orElseThrow(() -> new RuntimeException("Chave de confirmação não encontrada!"));
        User user = userRepository.findByEmail(confirmation.getUser().getEmail());
        user.setEnabled(true);
        userRepository.save(user);
        confirmationRepository.delete(confirmation);
    }

}
