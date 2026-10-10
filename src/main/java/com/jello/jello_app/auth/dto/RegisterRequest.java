package com.jello.jello_app.auth.dto;

import lombok.Data;

import java.sql.Blob;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}
