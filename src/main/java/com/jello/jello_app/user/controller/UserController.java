package com.jello.jello_app.user.controller;

import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.security.user.AppUserDetails;
import com.jello.jello_app.user.dto.ChangePasswordRequestDTO;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.StorageDTO;
import com.jello.jello_app.user.dto.UpdateUserRequestDTO;
import com.jello.jello_app.user.dto.UserSummaryDTO;
import com.jello.jello_app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping("/{userId}")
    public ProfileDTO getUserById(@PathVariable Long userId, @AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getUserProfile(userId, userDetails.getId());
    }

    // TODO: DELETAR ESSE ENDOPOINT E USAR O UNIVERSAO DE PEGAR PELO ID COM O ID DO USUARIO LOGADO
    @GetMapping("/profile")
    public ProfileDTO getUserProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getUserProfile(userDetails.getId(), userDetails.getId());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO updateUser(
            @Valid @ModelAttribute UpdateUserRequestDTO request,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return userService.updateUser(request, userDetails.getId());
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        userService.changePassword(userDetails.getId(), request);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/suggestions")
    public List<UserSummaryDTO> getSuggestions(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return userService.getSuggestions(userDetails.getId(), limit);
    }

    @GetMapping("/search")
    public List<UserSummaryDTO> searchUsers(
            @RequestParam(name = "q", defaultValue = "") String term,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return userService.searchUsers(term, userDetails.getId());
    }

    @GetMapping("/{userId}/followers")
    public List<UserSummaryDTO> getFollowers(@PathVariable Long userId, @AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getFollowers(userId, userDetails.getId());
    }

    @GetMapping("/{userId}/following")
    public List<UserSummaryDTO> getFollowing(@PathVariable Long userId, @AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getFollowing(userId, userDetails.getId());
    }

    @GetMapping("/banned")
    @PreAuthorize("hasRole('MODERATOR')")
    public List<UserSummaryDTO> getBannedUsers(@AuthenticationPrincipal AppUserDetails userDetails) {
        return userService.getBannedUsers(userDetails.getId());
    }

    @PostMapping("/{userId}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void banUser(@PathVariable Long userId, @AuthenticationPrincipal AppUserDetails userDetails) {
        userService.banUser(userId, userDetails.getId());
    }

    @DeleteMapping("/{userId}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbanUser(@PathVariable Long userId) {
        userService.unbanUser(userId);
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

    @GetMapping("/storage")
    @PreAuthorize("isAuthenticated()")
    public StorageDTO getUserStorage(
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return userService.getUserStorage(userDetails.getId());
    }
}
