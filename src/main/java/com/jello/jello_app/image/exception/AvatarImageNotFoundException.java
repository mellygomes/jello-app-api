package com.jello.jello_app.image.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class AvatarImageNotFoundException extends ResourceNotFoundException {
    public AvatarImageNotFoundException(Long id) {
        super("Imagem de perfil com ID " + id + " não encontrada.");
    }
}
