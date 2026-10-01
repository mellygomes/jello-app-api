package com.jello.jello_app.moderator.controller;

import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse> grantModerator(@PathVariable Long userId) {
        User user = userRoleService.grantModerator(userId);

        UserDTO userDto = UserMapper.toDto(user);
        return ResponseEntity.ok(new ApiResponse("Usuário inserido como Moderador!", userDto));
    }

    @PostMapping("/{userId}/revoke")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse> revokeModerator(@PathVariable Long userId) {
        User user = userRoleService.revokeModerator(userId);

        UserDTO userDto = UserMapper.toDto(user);
        return ResponseEntity.ok(new ApiResponse("Moderador atualizado como usuário comum!", userDto));
    }
}
