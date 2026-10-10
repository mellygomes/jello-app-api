package com.jello.jello_app.follow.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class FollowAlreadyFollowedException extends BusinessException {
    public FollowAlreadyFollowedException(Long id) {
        super("Não é possível seguir um usuário já seguido. ID: " + id);
    }
}
