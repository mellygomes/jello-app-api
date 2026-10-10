package com.jello.jello_app.user.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String username, String email) {
        super("Já existe um registro com o valor informado para o usuário: " + username + " ou e-mail: " + email);
    }
}
