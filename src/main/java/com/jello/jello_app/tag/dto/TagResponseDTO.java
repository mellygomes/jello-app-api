package com.jello.jello_app.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TagResponseDTO {

    private Long id;
    private String name;
    private String color;

}
