package com.jello.jello_app.user.controller;

import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UpdateUserRequestDTO;
import com.jello.jello_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final PostService postService;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    public ProfileDTO getUserById(@PathVariable Long userId) {
//        User user = userService.getUserById(userId);
//        return UserMapper.toDto(user);
        return userService.getUserProfile(userId);
    }

    // TODO: DELETAR ESSE ENDOPOINT E USAR O UNIVERSAO DE PEGAR PELO ID COM O ID DO USUARIO LOGADO
    @GetMapping("/profile")
    public ProfileDTO getUserProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getUserProfile(userDetails.getId());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO updateUser(
            @ModelAttribute UpdateUserRequestDTO request,
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

    @GetMapping("/{userId}/posts")
    public Page<PostResponseDTO> getAllPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute FilterRequestDTO request
    ) {
        return postService.getAllPostsByUser(userId, request, page, size);
    }
}
