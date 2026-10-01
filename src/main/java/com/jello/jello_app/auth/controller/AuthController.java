package com.jello.jello_app.auth.controller;

import com.jello.jello_app.auth.dto.LoginRequest;
import com.jello.jello_app.auth.dto.RegisterRequest;
import com.jello.jello_app.auth.dto.UserResponseDTO;
import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.security.jwt.JwtUtils;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authService.login(request);

            String jwt = jwtUtils.generateTokenForUser(authentication);
            ResponseCookie cookie = buildResponseCookie(jwt, Duration.ofHours(1));

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            UserDTO userDTO = UserMapper.toDto(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Registered!", userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        ResponseCookie cleanCookie = buildResponseCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(new ApiResponse("Logout realizado com sucesso mesmo meu dog!", null));
    }

    // Valida usuario logado e retorna dados para usar no front
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(@AuthenticationPrincipal AppUserDetails userDetails) {

        if (userDetails != null) {
            UserResponseDTO responseDTO = new UserResponseDTO(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail()
            );

            return ResponseEntity.ok().body(new ApiResponse("Usuário autenticado", responseDTO));
        }

        return ResponseEntity.ok().body(null);
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
