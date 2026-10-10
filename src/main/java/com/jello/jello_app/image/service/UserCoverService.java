package com.jello.jello_app.image.service;

import com.jello.jello_app.image.exception.CoverImageNotFoundException;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.repository.UserCoverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserCoverService {

    private final UserCoverRepository coverRepository;

    public UserCover getCoverById(Long coverId) {
        return coverRepository.findById(coverId)
                .orElseThrow(() -> new CoverImageNotFoundException(coverId));
    }

    public UserCover createUserCover() {
        ClassPathResource imgResource = new ClassPathResource("static/images/default-cover.png");
        String fileName = imgResource.getFilename();
        String fileType = "image/png";

        try {
            long fileSize = imgResource.contentLength();
            byte[] bytes = imgResource.getInputStream().readAllBytes();

            UserCover cover = ImageMapper.toUserCover(fileName, fileType, fileSize, bytes);

            return coverRepository.save(cover);
        } catch (IOException ex) {
            throw new FileReadException(fileName, ex);
        }
    }

    public UserCover updateUserCover(UserCover cover, MultipartFile image) {
        try {

            cover.setFileName(image.getOriginalFilename());
            cover.setFileType(image.getContentType());
            cover.setFileSize(image.getSize());
            cover.setData(image.getBytes());

            return coverRepository.save(cover);
        } catch (IOException ex) {
            throw new FileReadException(cover.getFileName(), ex);
        }
    }
}
