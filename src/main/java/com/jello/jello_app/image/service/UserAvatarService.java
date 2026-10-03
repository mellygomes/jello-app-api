package com.jello.jello_app.image.service;

import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.repository.UserAvatarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserAvatarRepository avatarRepository;

    public UserAvatar getAvatarById(Long avatarId) {
        return avatarRepository.findById(avatarId)
                .orElseThrow(() -> new RuntimeException("Imagem de perfil não encontrada!"));
    }

    public UserAvatar createUserAvatar() {
        try {
            ClassPathResource imgResource = new ClassPathResource("static/images/default-avatar.png");

            byte[] bytes = imgResource.getInputStream().readAllBytes();
            String fileName = imgResource.getFilename();
            String fileType = "image/png";
            long fileSize = imgResource.contentLength();

            UserAvatar avatar = ImageMapper.toUserAvatar(fileName, fileType, fileSize, bytes);

            return avatarRepository.save(avatar);

        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar e salvar a imagem de perfil do usuário");
        }
    }

    public UserAvatar updateUserAvatar(UserAvatar avatar, MultipartFile image) {

        try {

            avatar.setFileName(image.getOriginalFilename());
            avatar.setFileType(image.getContentType());
            avatar.setFileSize(image.getSize());
            avatar.setData(image.getBytes());

            return avatarRepository.save(avatar);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar imagem de perfil!");
        }
    }
}
