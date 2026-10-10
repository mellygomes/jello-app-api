package com.jello.jello_app.tag.service;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.exception.TagNotFoundException;
import com.jello.jello_app.tag.mapper.TagMapper;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<Tag> getAllTagsById(Set<Long> ids) {
        List<Tag> tags = tagRepository.findAllById(ids);

        if (tags.size() != ids.size()) {
            Set<Long> found = tags.stream().map(Tag::getId).collect(Collectors.toSet());
            Set<Long> notFound = new HashSet<>(ids);
            notFound.removeAll(found);

            throw new TagNotFoundException(notFound);
        }

        return tags;
    }
}
