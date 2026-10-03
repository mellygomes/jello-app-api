package com.jello.jello_app.image.service;

import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.model.PostImage;
import com.jello.jello_app.image.repository.PostImageRepository;
import com.jello.jello_app.post.model.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @Mock
    private PostImageRepository imageRepository;

    @InjectMocks
    private PostImageService postImageService;

    // Testa o save da imagem
    @Test
    void shouldSaveImage() {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("Super POST mesmo");

        MultipartFile file1 = new MockMultipartFile(
                "file1",
                "foto-um.png",
                "image/png",
                "conteudo mesmo".getBytes()
        );

        MultipartFile file2 = new MockMultipartFile(
                "file2",
                "foto-dois.png",
                "image/png",
                "conteudo 2 mesmo".getBytes()
        );

        List<MultipartFile> files = List.of(file1, file2);

        when(imageRepository.save(any(PostImage.class))).thenAnswer(invocation -> {
            PostImage savedImage = invocation.getArgument(0);
            if (savedImage.getId() == null) {
                savedImage.setId(1L);
            }
            return savedImage;
        });

        List<ImageDTO> result = postImageService.saveImageForPost(files, post);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("foto-um.png", result.get(0).getFileName());

        verify(imageRepository, times(2)).save(any(PostImage.class));
    }

    // Testa o save da imagem quando gera erro ao ler algum arquivo
    @Test
    void shouldThrowExceptionWhenFileReadsFail() throws IOException {
        Post post = new Post();

        MultipartFile corruptFile = mock(MultipartFile.class);
        when(corruptFile.getOriginalFilename()).thenReturn("corrompido-mesmo.png");
        when(corruptFile.getBytes()).thenThrow(new IOException("Erro ao ler o arquivo"));

        List<MultipartFile> files = List.of(corruptFile);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postImageService.saveImageForPost(files, post)
        );

        assertTrue(exception.getMessage().contains("Erro ao processar a imagem: corrompido-mesmo.png"));
        verify(imageRepository, never()).save(any());
    }

    // Testa a recuperação da imagem por ID
    @Test
    void shouldGetImageById() {
        Long id = 1L;
        PostImage imageMock = new PostImage();
        imageMock.setId(id);
        imageMock.setFileName("super-imagem-mesmo.png");

        when(imageRepository.findById(id)).thenReturn(Optional.of(imageMock));

        PostImage image = postImageService.getImageById(id);

        assertEquals(imageMock.getFileName(), image.getFileName());

        verify(imageRepository, times(1)).findById(anyLong());
    }

    // Testa a recuperação de imagem quando não existe ou ID inválido
    @Test
    void shouldThrowExceptionToGetImageWhenInvalidId() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> postImageService.getImageById(1L));

        assertEquals("Imagem não encontrada!", exception.getMessage());

        verify(imageRepository, times(1)).findById(anyLong());
    }
}