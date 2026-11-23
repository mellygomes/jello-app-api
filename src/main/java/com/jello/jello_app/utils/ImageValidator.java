package com.jello.jello_app.utils;

import com.jello.jello_app.image.exception.FileReadException;
import com.jello.jello_app.image.exception.InvalidImageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// Confere se o arquivo enviado e mesmo uma imagem (jpeg, png, webp ou gif) olhando os primeiros bytes
public final class ImageValidator {

    public record ValidatedImage(String contentType, String fileName, byte[] bytes) {
    }

    private ImageValidator() {
    }

    public static ValidatedImage validate(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new FileReadException(originalName, ex);
        }

        if (bytes.length == 0) {
            throw new InvalidImageException("O arquivo de imagem está vazio.");
        }

        String contentType = detectType(bytes);
        if (contentType == null) {
            throw new InvalidImageException("Formato de imagem não suportado. Use JPG, PNG, WEBP ou GIF.");
        }

        return new ValidatedImage(contentType, safeName(originalName, contentType), bytes);
    }

    private static String detectType(byte[] b) {
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "image/gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    // Tira caminho e caracteres estranhos do nome (ele volta num header de download)
    private static String safeName(String originalName, String contentType) {
        String name = originalName == null ? "" : originalName;
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");

        if (name.length() > 100) {
            name = name.substring(name.length() - 100);
        }
        if (name.isBlank() || name.chars().allMatch(c -> c == '.' || c == '_')) {
            name = "imagem." + contentType.substring("image/".length());
        }
        return name;
    }
}
