package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.service.ConfirmationService;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import com.jello.jello_app.user.service.UserService;
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
    private final UserService userService;
    private final ConfirmationService confirmationService;

    public Authentication login(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        if (userDetails != null && userDetails.getId() != null)
            updateLastLogin(userDetails.getId());

        return authentication;
    }

    public void updateLastLogin(Long userId) {
        User user = userService.getUserById(userId);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "";
        if (authentication != null && !authentication.getName().isBlank())
            username = authentication.getName();

        return userRepository.findByUsername(username);
    }

    public void verifyAccountKey(String token) {
        Confirmation confirmation = confirmationService.getConfirmationByKey(token);

        User user = userService.getUserById(confirmation.getUser().getId());
        user.setEnabled(true);

        userRepository.save(user);
        confirmationService.deleteConfirmation(confirmation);
    }

}
