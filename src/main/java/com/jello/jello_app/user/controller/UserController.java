package com.jello.jello_app.user.controller;

import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.user.dto.UpdateUserRequest;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            UserDTO userDto = UserMapper.toDto(user);
            return ResponseEntity.ok(new ApiResponse("Success!", userDto));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Long userId) {
        try {
            User user = userService.updateUser(request, userId);
            UserDTO userDto = UserMapper.toDto(user);
            return ResponseEntity.ok(new ApiResponse("Updated!", userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse("Delete success!", null));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }


}
