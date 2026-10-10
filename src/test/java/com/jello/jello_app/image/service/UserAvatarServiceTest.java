package com.jello.jello_app.image.service;

import com.jello.jello_app.image.exception.AvatarImageNotFoundException;
import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.repository.UserAvatarRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    @Mock
    private UserAvatarRepository avatarRepository;

    @InjectMocks
    private UserAvatarService avatarService;

    private UserAvatar avatar;

    @BeforeEach
    void setUp() {
        avatar = createAvatar();
    }

    @Test
    void shouldGetAvatarById() {
        Long id = 1L;

        when(avatarRepository.findById(id)).thenReturn(Optional.of(avatar));

        UserAvatar result = avatarService.getAvatarById(id);

        assertEquals(avatar.getFileName(), result.getFileName());
        verify(avatarRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenAvatarNotFound() {
        Long id = 1L;
        when(avatarRepository.findById(id)).thenThrow(new AvatarImageNotFoundException(id));

        AvatarImageNotFoundException exception = assertThrows(
                AvatarImageNotFoundException.class,
                () -> avatarService.getAvatarById(id)
        );

        assertTrue(exception.getMessage().contains("Imagem de perfil com ID 1 não encontrada."));
        verify(avatarRepository).findById(anyLong());
    }

    @Test
    void shouldCreateUserAvatar() {
        when(avatarRepository.save(any())).thenReturn(avatar);

        UserAvatar result = avatarService.createUserAvatar();

        assertEquals(avatar.getFileName(), result.getFileName());
        verify(avatarRepository).save(any());
    }

    @Test
    void shouldUpdateUserAvatar() {
        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "conteudo mesmo".getBytes()
        );

        when(avatarRepository.save(any())).thenReturn(avatar);

        UserAvatar result = avatarService.updateUserAvatar(avatar, file);

        assertTrue(result.getFileName().contains("avatar.png"));
        assertEquals(file.getContentType(), result.getFileType());
        verify(avatarRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserAvatarUpdateFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getBytes()).thenThrow(new IOException("error"));

        FileReadException exception = assertThrows(
                FileReadException.class,
                () -> avatarService.updateUserAvatar(avatar, file)
        );

        assertTrue(exception.getMessage().contains("avatar.png"));
        verifyNoInteractions(avatarRepository);
    }

    private UserAvatar createAvatar() {
        UserAvatar avatar = new UserAvatar();
        avatar.setId(1L);
        avatar.setFileName("avatar.png");
        return avatar;
    }
}