package com.jello.jello_app.role.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModeratorResponseDTO {
    private Long id;
    private Long userId;
}
