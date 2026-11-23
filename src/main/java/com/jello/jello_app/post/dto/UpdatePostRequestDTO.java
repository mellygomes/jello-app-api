package com.jello.jello_app.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
    private String title;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String content;
}
