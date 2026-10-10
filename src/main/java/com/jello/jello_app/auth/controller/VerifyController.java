package com.jello.jello_app.auth.controller;

import com.jello.jello_app.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verify")
public class VerifyController {

    private final AuthService authService;

    @GetMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyAccount(@RequestParam String token) {
        authService.verifyAccountKey(token);
    }
}
