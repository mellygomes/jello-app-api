package com.jello.jello_app.confirmation.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class ConfirmationNotFoundException extends ResourceNotFoundException {
    public ConfirmationNotFoundException(String key) {
        super("Chave de confirmação não encontrada: " + key);
    }
}
