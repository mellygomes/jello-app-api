package com.jello.jello_app.role.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class UserAlreadyHasModeratorException extends BusinessException {
    public UserAlreadyHasModeratorException(String username) {
        super("O usuário " + username + " já possui cargo de moderador.");
    }
}
