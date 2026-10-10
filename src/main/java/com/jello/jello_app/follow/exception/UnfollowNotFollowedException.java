package com.jello.jello_app.follow.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class UnfollowNotFollowedException extends BusinessException {
    public UnfollowNotFollowedException(Long id) {
        super("Não é possível deixar de seguir um usuário que não segue. ID: " + id);
    }
}
