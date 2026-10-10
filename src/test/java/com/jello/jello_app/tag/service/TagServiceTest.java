package com.jello.jello_app.tag.service;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.exception.TagNotFoundException;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.tag.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void shouldFindAllTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag(1L, "Pintura Digital", "#00B3FF", null));

        when(tagRepository.findAll()).thenReturn(tags);

        List<TagResponseDTO> result = tagService.findAll();

        assertEquals(tags.getFirst().getName(), result.getFirst().getName());

        verify(tagRepository).findAll();
    }

    @Test
    void shouldReturnEmptyList() {
        when(tagRepository.findAll()).thenReturn(List.of());
        List<TagResponseDTO> result = tagService.findAll();

        assertTrue(result.isEmpty());
        verify(tagRepository).findAll();
    }

    @Test
    void shouldGetAllTagsById() {
        Set<Long> ids = Set.of(7L, 9L);
        List<Tag> tags = List.of(
                new Tag(7L, "Pintura Digital", "#00B3FF", null),
                new Tag(9L, "Pixel Art", "#00F5A0", null)
        );
        when(tagRepository.findAllById(ids)).thenReturn(tags);

        List<Tag> result = tagService.getAllTagsById(ids);

        assertEquals(2, result.size());
        assertTrue(result.getFirst().getName().contains("Pintura Digital"));

        verify(tagRepository).findAllById(any());
    }

    @Test
    void shouldThrowExceptionWhenGetTagsByInvalidId() {
        Set<Long> ids = Set.of(1L, 2L, 3L);
        List<Tag> tags = List.of(
                new Tag(1L, "Pintura Digital", "#00B3FF", null),
                new Tag(2L, "Pixel Art", "#00F5A0", null)
        );

        when(tagRepository.findAllById(ids)).thenReturn(tags);

        TagNotFoundException exception = assertThrows(
                TagNotFoundException.class,
                () -> tagService.getAllTagsById(ids)
        );

        assertEquals("Tags não encontradas: [3]", exception.getMessage());

        verify(tagRepository).findAllById(any());
    }
}