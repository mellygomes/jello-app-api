package com.jello.jello_app.image.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageDTO {
    private Long id;
    private String fileName;
}
