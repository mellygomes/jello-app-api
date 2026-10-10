package com.jello.jello_app.auth.controller;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.auth.dto.RegisterRequest;
import com.jello.jello_app.auth.dto.UserResponseDTO;
import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.security.jwt.JwtUtils;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        Authentication authentication = authService.login(request);
        String jwt = jwtUtils.generateTokenForUser(authentication);
        ResponseCookie cookie = buildResponseCookie(jwt, Duration.ofHours(1));

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return UserMapper.toDto(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cleanCookie = buildResponseCookie("", Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleanCookie.toString()).build();
    }

    // Valida usuario logado e retorna dados para usar no front
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal AppUserDetails userDetails) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        if (userDetails != null) {
            responseDTO.setId(userDetails.getId());
            responseDTO.setUsername(userDetails.getUsername());
            responseDTO.setEmail(userDetails.getEmail());

        }
        return responseDTO;
    }

    private ResponseCookie buildResponseCookie(String jwt, Duration maxAge) {
        return ResponseCookie
                .from("access_token", jwt)
                .httpOnly(true)
                // secure setado como false para desenvolvimento
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
