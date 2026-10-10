package com.jello.jello_app.role.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class RoleNotFoundException extends ResourceNotFoundException {
    public RoleNotFoundException(String role) {
        super("Cargo não encontrado: " + role);
    }
}
