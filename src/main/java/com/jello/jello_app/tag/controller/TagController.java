package com.jello.jello_app.tag.controller;

import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTags() {
        try {
            List<TagResponseDTO> tags = tagService.findAll();
            return ResponseEntity.ok().body(new ApiResponse("Tags carregadas com sucesso!", tags));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
