package com.jello.jello_app.role.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class ModeratorNotFoundException extends ResourceNotFoundException {
    public ModeratorNotFoundException() {
        super("Moderador não encontrado");
    }
}
