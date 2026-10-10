package com.jello.jello_app.follow.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class SelfFollowException extends BusinessException {
    public SelfFollowException(Long id) {
        super("Usuário com ID " + id + " não pode seguir a si mesmo.");
    }
}
