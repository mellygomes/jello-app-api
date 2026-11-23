package com.jello.jello_app.role.mapper;

import com.jello.jello_app.role.dto.ModeratorResponseDTO;
import com.jello.jello_app.role.model.Moderator;

public class ModeratorMapper {
    public static ModeratorResponseDTO toDTO(Moderator moderator) {
        return ModeratorResponseDTO.builder()
                .id(moderator.getId())
                .userId(moderator.getId())
                .build();
    }
}
