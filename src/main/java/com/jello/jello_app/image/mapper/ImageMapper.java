package com.jello.jello_app.image.mapper;

import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.model.PostImage;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.post.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImageMapper {
    public static ImageDTO toDto(PostImage image) {
        return ImageDTO.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .build();
    }

    public static PostImage toPostImage(MultipartFile file, Post post) throws IOException {
        return PostImage.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .data(file.getBytes())
                .post(post)
                .build();
    }

    public static UserAvatar toUserAvatar(String fileName, String fileType, long fileSize, byte[] data) {
        return UserAvatar.builder()
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .data(data)
                .build();
    }

    public static UserCover toUserCover(String fileName, String fileType, long fileSize, byte[] data) {
        return UserCover.builder()
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .data(data)
                .build();
    }
}
