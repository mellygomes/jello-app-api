package com.jello.jello_app.tag.controller;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.service.TagService;
import lombok.RequiredArgsConstructor;
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
    public List<TagResponseDTO> getAllTags() {
        return tagService.findAll();
    }
}
