package com.jello.jello_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Informe a senha atual")
    private String currentPassword;

    @NotBlank(message = "Informe a nova senha")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    private String newPassword;
}
