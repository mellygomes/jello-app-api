package com.jello.jello_app.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiVoteResponseDTO {

    // quantos acham que e IA / arte humana e o voto de quem pediu (null = sem voto)
    private int ai;
    private int human;
    private Boolean myVote;

}
