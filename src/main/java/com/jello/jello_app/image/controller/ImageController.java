package com.jello.jello_app.image.controller;

import com.jello.jello_app.image.model.Image;
import com.jello.jello_app.image.repository.ImageRepository;
import com.jello.jello_app.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/images")
public class ImageController {
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @GetMapping("/{imageId}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId){
        Image image = imageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(image.getData());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }
}
