package com.jello.jello_app.image.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class CoverImageNotFoundException extends ResourceNotFoundException {
    public CoverImageNotFoundException(Long id) {
        super("Imagem de capa com ID " + id + " não encontrada.");
    }
}
