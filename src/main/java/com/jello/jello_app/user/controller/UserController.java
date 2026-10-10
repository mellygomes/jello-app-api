package com.jello.jello_app.user.controller;

import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UpdateUserRequest;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    public UserDTO getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return UserMapper.toDto(user);
    }

    @GetMapping("/profile")
    public ProfileDTO getUserProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getUserProfile(userDetails.getId());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO updateUser(
            @ModelAttribute UpdateUserRequest request,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return userService.updateUser(request, userDetails.getId());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
