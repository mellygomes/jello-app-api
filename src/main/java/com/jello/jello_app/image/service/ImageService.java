package com.jello.jello_app.image.service;

import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.model.Image;
import com.jello.jello_app.image.repository.ImageRepository;
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
public class ImageService {

    private final ImageRepository imageRepository;

    @Transactional
    public List<ImageDTO> saveImageForPost(List<MultipartFile> files, Post post) {
        List<ImageDTO> savedImagesDTO = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = ImageMapper.toEntity(file, post);
                Image savedImage = imageRepository.save(image);
                ImageDTO imageDTO = ImageMapper.toDto(savedImage);

                savedImagesDTO.add(imageDTO);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar a imagem: " + file.getOriginalFilename(), e);
            }
        }

        return savedImagesDTO;
    }

    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada!"));
    }
}
