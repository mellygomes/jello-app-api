package com.jello.jello_app.image.service;

import com.jello.jello_app.image.exception.AvatarImageNotFoundException;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.repository.UserAvatarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.jello.jello_app.utils.ImageValidator;
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

    // Apaga a foto do banco (usado quando a conta e excluida)
    public void deleteAvatar(UserAvatar avatar) {
        if (avatar != null) avatarRepository.delete(avatar);
    }

    public UserAvatar updateUserAvatar(UserAvatar avatar, MultipartFile image) {
        ImageValidator.ValidatedImage validated = ImageValidator.validate(image);

        avatar.setFileName(validated.fileName());
        avatar.setFileType(validated.contentType());
        avatar.setFileSize((long) validated.bytes().length);
        avatar.setData(validated.bytes());

        return avatarRepository.save(avatar);
    }
}
