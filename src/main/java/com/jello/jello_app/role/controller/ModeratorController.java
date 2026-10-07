package com.jello.jello_app.role.controller;

import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/moderator")
public class ModeratorController {

    private final UserRoleService userRoleService;

    @PostMapping("/{userId}/grant")
    @PreAuthorize("hasRole('MODERATOR')")
    public UserDTO grantModerator(@PathVariable Long userId) {
        User user = userRoleService.grantModerator(userId);
        return UserMapper.toDto(user);
    }

    @PostMapping("/{userId}/revoke")
    @PreAuthorize("hasRole('MODERATOR')")
    public UserDTO revokeModerator(@PathVariable Long userId) {
        User user = userRoleService.revokeModerator(userId);
        return UserMapper.toDto(user);
    }
}
