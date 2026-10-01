package com.jello.jello_app.image.service;

import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.repository.UserCoverRepository;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UserCoverService {

    private final UserCoverRepository coverRepository;
    private final UserRepository userRepository;

    public UserCover getCoverById(Long coverId) {
        return coverRepository.findById(coverId)
                .orElseThrow(() -> new RuntimeException("Imagem de capa não encontrada!"));
    }

    public UserCover createUserCover() {
        try {
            ClassPathResource imgResource = new ClassPathResource("static/images/default-cover.png");

            byte[] bytes = imgResource.getInputStream().readAllBytes();
            String fileName = imgResource.getFilename();
            String fileType = "image/png";
            long fileSize = imgResource.contentLength();

            UserCover cover = new UserCover();
            cover.setFileName(fileName);
            cover.setFileType(fileType);
            cover.setFileSize(fileSize);
            cover.setData(bytes);

            return coverRepository.save(cover);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar imagem de capa de perfil");
        }
    }

    public UserCover updateUserCover(UserCover cover, MultipartFile image) {
        try {
            // TODO: IMPLEMENTAR ISSO TALVEZ PASSANDO O USUARIO OU O ID DA IMAGEM DIREITO
            // PARA NAO DAR PROBLEMA DE INJECAO CIRCULAR E PEGAR O ID DO USUARIO QUE JA EXISTE
            // FAZER ISSO TANTO PARA COVER QUANTO PARA AVATAR E APARTIR DAI ATUALIZAR AMBOS PELA REQUEST
//            AvatarCov user = userRepository.findById(avatarId);
//            UserCover cover = coverRepository.findById(user.getProfileCover().getId())
//                    .orElseThrow(() -> new RuntimeException("Imagem de capa não encontrada"));

            cover.setFileName(image.getOriginalFilename());
            cover.setFileType(image.getContentType());
            cover.setFileSize(image.getSize());
            cover.setData(image.getBytes());

            return coverRepository.save(cover);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar imagem de capa de perfil");
        }
    }
}
