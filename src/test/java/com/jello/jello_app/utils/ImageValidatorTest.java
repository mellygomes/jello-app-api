package com.jello.jello_app.utils;

import com.jello.jello_app.image.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @Test
    void shouldAcceptPngAndUseDetectedType() {
        // o content-type mentido do cliente e ignorado
        MockMultipartFile file = new MockMultipartFile("f", "foto.png", "text/html", PNG);

        ImageValidator.ValidatedImage result = ImageValidator.validate(file);

        assertEquals("image/png", result.contentType());
        assertEquals("foto.png", result.fileName());
    }

    @Test
    void shouldAcceptJpeg() {
        MockMultipartFile file = new MockMultipartFile("f", "foto.jpg", "image/jpeg", JPEG);

        assertEquals("image/jpeg", ImageValidator.validate(file).contentType());
    }

    @Test
    void shouldRejectNonImageEvenWithImageContentType() {
        MockMultipartFile file = new MockMultipartFile("f", "script.png", "image/png", "<script>alert(1)</script>".getBytes());

        assertThrows(InvalidImageException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("f", "vazio.png", "image/png", new byte[0]);

        assertThrows(InvalidImageException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void shouldCleanFileName() {
        MockMultipartFile file = new MockMultipartFile("f", "..\\..\\pasta/minha foto\"x.png", "image/png", PNG);

        String name = ImageValidator.validate(file).fileName();

        assertEquals("minha_foto_x.png", name);
    }
}
