package com.jello.jello_app.role.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class UserDoesNotHaveRoleException extends BusinessException {
    public UserDoesNotHaveRoleException(Long userId, String roleType) {
        super("O usuário com ID " + userId + " não possui o cargo " + roleType + " que foi solicitado para remoção.");
    }
}
