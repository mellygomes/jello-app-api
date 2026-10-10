package com.jello.jello_app.user.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long id) {
        super("Usuário não encontrado: ID " + id);
    }
}
