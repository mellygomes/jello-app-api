package com.jello.jello_app.auth.dto;

public record UserResponseDTO (
        Long id,
        String username,
        String email
) {}
