package com.jello.jello_app.post.dto;

import com.jello.jello_app.enumeration.PostClassification;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassificationRequestDTO {

    @NotNull(message = "Informe a classificação (AI, HUMAN ou NONE)")
    private PostClassification classification;
}
