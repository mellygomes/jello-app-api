package com.jello.jello_app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 46, message = "O nome deve ter no máximo 46 caracteres")
    private String firstName;

    @Size(max = 46, message = "O sobrenome deve ter no máximo 46 caracteres")
    private String lastName;

    @Size(max = 255, message = "A bio deve ter no máximo 255 caracteres")
    private String bio;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres")
    private String email;

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 46, message = "O nome de usuário deve ter entre 3 e 46 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Use apenas letras, números, ponto, hífen e underline no nome de usuário")
    private String username;
//    private String password;
    private MultipartFile avatar;
    private MultipartFile cover;

}
