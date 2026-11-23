package com.jello.jello_app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "Informe o usuário")
    @Size(max = 255)
    private String username;

    @NotBlank(message = "Informe a senha")
    @Size(max = 255)
    private String password;
}
