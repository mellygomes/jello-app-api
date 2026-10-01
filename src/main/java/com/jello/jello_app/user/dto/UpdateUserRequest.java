package com.jello.jello_app.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String bio;
    private String email;
    private String username;
    private String password;
    private MultipartFile avatar;
    private MultipartFile cover;

}
