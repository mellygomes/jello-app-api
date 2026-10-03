package com.jello.jello_app.image.controller;

import com.jello.jello_app.image.model.PostImage;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.service.PostImageService;
import com.jello.jello_app.image.service.UserAvatarService;
import com.jello.jello_app.image.service.UserCoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/images")
public class ImageController {

    private final PostImageService postImageService;
    private final UserAvatarService avatarService;
    private final UserCoverService coverService;

    @GetMapping("/{imageId}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) {
        PostImage image = postImageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(image.getData());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/avatars/{avatarId}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long avatarId) {
        UserAvatar avatar = avatarService.getAvatarById(avatarId);
        ByteArrayResource resource = new ByteArrayResource(avatar.getData());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(avatar.getFileType()))
                .body(resource);
    }

    @GetMapping("/covers/{coverId}")
    public ResponseEntity<Resource> getProfileCover(@PathVariable Long coverId) {
        UserCover cover = coverService.getCoverById(coverId);
        ByteArrayResource resource = new ByteArrayResource(cover.getData());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(cover.getFileType()))
                .body(resource);
    }
}
