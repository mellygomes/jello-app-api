package com.jello.jello_app.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddCommentRequestDTO {

    @NotBlank(message = "O comentário não pode ser vazio")
    @Size(max = 255, message = "O comentário deve ter no máximo 255 caracteres")
    private String comment;

    // id do comentario que ta sendo respondido (opcional)
    private Long parentId;
}
