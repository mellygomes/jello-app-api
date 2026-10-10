package com.jello.jello_app.image.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class PostImageNotFoundException extends ResourceNotFoundException {
    public PostImageNotFoundException(Long id) {
        super("Imagem de post com ID " + id + " não encontrada.");
    }
}
