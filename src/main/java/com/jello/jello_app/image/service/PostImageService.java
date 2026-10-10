package com.jello.jello_app.image.service;

import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.exception.PostImageNotFoundException;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.PostImage;
import com.jello.jello_app.image.repository.PostImageRepository;
import com.jello.jello_app.post.model.Post;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageService {

    private final PostImageRepository imageRepository;

    @Transactional
    public List<ImageDTO> saveImageForPost(List<MultipartFile> files, Post post) {
        List<ImageDTO> savedImagesDTO = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                PostImage image = ImageMapper.toPostImage(file, post);
                PostImage savedImage = imageRepository.save(image);
                ImageDTO imageDTO = ImageMapper.toDto(savedImage);

                savedImagesDTO.add(imageDTO);
            } catch (IOException ex) {
                throw new FileReadException(file.getOriginalFilename(), ex);
            }
        }

        return savedImagesDTO;
    }

    public PostImage getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new PostImageNotFoundException(imageId));
    }
}
