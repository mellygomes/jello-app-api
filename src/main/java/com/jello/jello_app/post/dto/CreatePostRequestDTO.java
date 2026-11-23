package com.jello.jello_app.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreatePostRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
    private String title;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String content;

    @Size(max = 10, message = "Use no máximo 10 tags")
    private Set<Long> tagIds;
}
