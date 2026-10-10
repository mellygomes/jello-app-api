package com.jello.jello_app.image.service;

import com.jello.jello_app.image.exception.AvatarImageNotFoundException;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.repository.UserAvatarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserAvatarRepository avatarRepository;

    public UserAvatar getAvatarById(Long avatarId) {
        return avatarRepository.findById(avatarId)
                .orElseThrow(() -> new AvatarImageNotFoundException(avatarId));
    }

    public UserAvatar createUserAvatar() {
        ClassPathResource imgResource = new ClassPathResource("static/images/default-avatar.png");
        String fileName = imgResource.getFilename();
        String fileType = "image/png";
        byte[] bytes;
        long fileSize;

        try (InputStream in = imgResource.getInputStream()) {

            fileSize = imgResource.contentLength();
            bytes = in.readAllBytes();

        } catch (IOException ex) {
            throw new FileReadException(fileName, ex);
        }

        UserAvatar avatar = ImageMapper.toUserAvatar(fileName, fileType, fileSize, bytes);
        return avatarRepository.save(avatar);
    }

    public UserAvatar updateUserAvatar(UserAvatar avatar, MultipartFile image) {

        try {

            avatar.setFileName(image.getOriginalFilename());
            avatar.setFileType(image.getContentType());
            avatar.setFileSize(image.getSize());
            avatar.setData(image.getBytes());

            return avatarRepository.save(avatar);

        } catch (IOException ex) {
            throw new FileReadException(avatar.getFileName(), ex);
        }
    }
}
