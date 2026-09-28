package com.jello.jello_app.role.service;

import com.jello.jello_app.user.model.User;

public interface UserRoleService {
    User grantModerator(Long userId);

    User revokeModerator(Long userId);
}
