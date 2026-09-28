package com.jello.jello_app.image.mapper;

import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.model.Image;
import com.jello.jello_app.post.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImageMapper {
    public static ImageDTO toDto(Image image) {
        return ImageDTO.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .build();
    }

    public static Image toEntity(MultipartFile file, Post post) throws IOException {
        return Image.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .data(file.getBytes())
                .post(post)
                .build();
    }
}
