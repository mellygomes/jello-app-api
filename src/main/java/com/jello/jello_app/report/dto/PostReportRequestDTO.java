package com.jello.jello_app.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostReportRequestDTO {
    @NotNull(message = "Informe a postagem denunciada")
    private Long postId;

    @NotBlank(message = "Descreva o motivo da denúncia")
    @Size(max = 1000, message = "A denúncia deve ter no máximo 1000 caracteres")
    private String content;
}
