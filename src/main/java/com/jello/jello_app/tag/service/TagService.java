package com.jello.jello_app.tag.service;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.mapper.TagMapper;
import com.jello.jello_app.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponseDTO> findAll() {
        return tagRepository.findAll()
                .stream()
                .map(TagMapper::toDto)
                .toList();
    }
}
