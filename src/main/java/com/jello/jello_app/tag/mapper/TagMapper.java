package com.jello.jello_app.tag.mapper;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.model.Tag;

public class TagMapper {
    public static TagResponseDTO toDto(Tag tag) {
        return TagResponseDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
