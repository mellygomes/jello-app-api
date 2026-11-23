package com.jello.jello_app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 46, message = "O nome deve ter no máximo 46 caracteres")
    private String firstName;

    @NotBlank(message = "O sobrenome é obrigatório")
    @Size(max = 46, message = "O sobrenome deve ter no máximo 46 caracteres")
    private String lastName;

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 46, message = "O nome de usuário deve ter entre 3 e 46 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Use apenas letras, números, ponto, hífen e underline no nome de usuário")
    private String username;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    private String password;
}
