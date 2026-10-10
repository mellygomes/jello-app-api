package com.jello.jello_app.image.service;

import com.jello.jello_app.image.exception.CoverImageNotFoundException;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.repository.UserCoverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCoverServiceTest {

    @Mock
    private UserCoverRepository coverRepository;

    @InjectMocks
    private UserCoverService coverService;

    private UserCover cover;

    @BeforeEach
    void setUp() {
        cover = createCover();
    }

    @Test
    void shouldGetCoverById() {
        Long id = 1L;

        when(coverRepository.findById(id)).thenReturn(Optional.of(cover));

        UserCover result = coverService.getCoverById(id);

        assertEquals(cover.getFileName(), result.getFileName());
        verify(coverRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenCoverNotFound() {
        Long id = 1L;
        when(coverRepository.findById(id)).thenThrow(new CoverImageNotFoundException(id));

        CoverImageNotFoundException exception = assertThrows(
                CoverImageNotFoundException.class,
                () -> coverService.getCoverById(id)
        );

        assertTrue(exception.getMessage().contains("Imagem de capa com ID 1 não encontrada."));
        verify(coverRepository).findById(anyLong());
    }

    @Test
    void shouldCreateUserAvatar() {
        when(coverRepository.save(any())).thenReturn(cover);

        UserCover result = coverService.createUserCover();

        assertEquals(cover.getFileName(), result.getFileName());
        verify(coverRepository).save(any());
    }

    @Test
    void shouldUpdateUserCover() {
        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "conteudo mesmo".getBytes()
        );

        when(coverRepository.save(any())).thenReturn(cover);

        UserCover result = coverService.updateUserCover(cover, file);

        assertTrue(result.getFileName().contains("avatar.png"));
        assertEquals(file.getContentType(), result.getFileType());
        verify(coverRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserCoverUpdateFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("cover-atualizada.png");
        when(file.getBytes()).thenThrow(new IOException("error"));

        FileReadException exception = assertThrows(
                FileReadException.class,
                () -> coverService.updateUserCover(cover, file)
        );

        assertTrue(exception.getMessage().contains("cover-atualizada.png"));
        verifyNoInteractions(coverRepository);
    }

    private UserCover createCover() {
        UserCover cover = new UserCover();
        cover.setId(1L);
        cover.setFileName("cover.png");

        return cover;
    }
}