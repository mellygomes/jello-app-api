package com.jello.jello_app.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiVoteRequestDTO {

    @NotNull(message = "Informe o voto (ai: true ou false)")
    private Boolean ai;
}
