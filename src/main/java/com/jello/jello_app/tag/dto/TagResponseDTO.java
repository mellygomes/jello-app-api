package com.jello.jello_app.tag.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagResponseDTO {

    private Long id;
    private String name;
    private String color;

}
