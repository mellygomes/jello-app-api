package com.jello.jello_app.auth.controller;

import com.jello.jello_app.auth.dto.LoginRequestDTO;
import com.jello.jello_app.auth.dto.RegisterRequestDTO;
import com.jello.jello_app.auth.dto.UserResponseDTO;
import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.auth.service.LoginAttemptService;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.security.jwt.JwtUtils;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final LoginAttemptService loginAttemptService;

    // secure = true quando o site roda em https (producao)
    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        String attemptKey = loginAttemptService.keyFor(request.getUsername(), httpRequest.getRemoteAddr());
        loginAttemptService.checkAllowed(attemptKey);

        Authentication authentication;
        try {
            authentication = authService.login(request);
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailure(attemptKey);
            throw ex;
        }

        loginAttemptService.reset(attemptKey);

        String jwt = jwtUtils.generateTokenForUser(authentication);
        ResponseCookie cookie = buildResponseCookie(jwt, Duration.ofHours(1));

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/register")
    public UserDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        User user = userService.register(request);
        return UserMapper.toDto(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cleanCookie = buildResponseCookie("", Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleanCookie.toString()).build();
    }

    // Valida usuario logado e retorna dados para usar no front (sem login = 401)
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal AppUserDetails userDetails) {
        User user = userService.getUserById(userDetails.getId());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarId(UserMapper.avatarId(user))
                .roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .moderator(user.hasRole(RoleType.ROLE_MODERATOR.getName()))
                .build();
    }

    private ResponseCookie buildResponseCookie(String jwt, Duration maxAge) {
        return ResponseCookie
                .from("access_token", jwt)
                .httpOnly(true)
                // secure fica false no desenvolvimento (http), ligar com auth.cookie.secure=true em producao
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite)
                .build();
    }
}
