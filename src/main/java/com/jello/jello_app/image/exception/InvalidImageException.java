package com.jello.jello_app.image.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class InvalidImageException extends BusinessException {
    public InvalidImageException(String message) {
        super(message);
    }
}
