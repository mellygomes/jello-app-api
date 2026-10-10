package com.jello.jello_app.follow.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class SelfUnfollowException extends BusinessException {
    public SelfUnfollowException(Long id) {
        super("Usuário com ID " + id + " não pode deixar de seguir a si mesmo.");
    }
}
