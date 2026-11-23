package com.jello.jello_app.post.controller;

import com.jello.jello_app.common.exception.BadRequestException;
import com.jello.jello_app.post.dto.*;
import com.jello.jello_app.post.service.PostAiVoteService;
import com.jello.jello_app.post.service.PostModerationService;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.security.user.AppUserDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/posts")
public class PostController {

    private static final int MAX_IMAGES = 10;

    private final PostService postService;
    private final PostAiVoteService postAiVoteService;
    private final PostModerationService postModerationService;
    private final Validator validator;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public PostResponseDTO createPost(@RequestPart(value = "images") List<MultipartFile> images,
                                      @RequestPart("post") String postRequest) {
        if (images.isEmpty() || images.size() > MAX_IMAGES) {
            throw new BadRequestException("Envie de 1 a " + MAX_IMAGES + " imagens.");
        }

        CreatePostRequestDTO request;
        try {
            request = new ObjectMapper().readValue(postRequest, CreatePostRequestDTO.class);
        } catch (JacksonException ex) {
            throw new BadRequestException("Dados da postagem inválidos.");
        }
        if (request == null) {
            throw new BadRequestException("Dados da postagem inválidos.");
        }

        Set<ConstraintViolation<CreatePostRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BadRequestException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }

        return postService.createPost(request, images);
    }

    @GetMapping("/{postId}")
    public PostResponseDTO getPost(@PathVariable Long postId) {
        return postService.getPostResponse(postId);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    public PostResponseDTO updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostRequestDTO request) {
        postService.updatePost(request, postId);
        return postService.getPostResponse(postId);
    }

    @PostMapping("/{postId}/likes")
    public void like(@PathVariable Long postId) {
        postService.addLike(postId);
    }

    @DeleteMapping("/{postId}/likes")
    public void unlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal AppUserDetails details
    ) {
        postService.removeLike(postId, details.getId());
    }

    @GetMapping("/list")
    public Page<PostResponseDTO> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute FilterRequestDTO filter
    ) {
        return postService.findPosts(filter, page, size);
    }

    @GetMapping("/explore")
    public Page<PostResponseDTO> explorePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute FilterRequestDTO filter
    ) {
        return postService.findExplorePosts(filter, page, size);
    }

    // Voto da comunidade: "acho que isso e IA" (ai = true) ou "e arte humana" (ai = false)
    @PutMapping("/{postId}/ai-vote")
    public AiVoteResponseDTO vote(@PathVariable Long postId, @Valid @RequestBody AiVoteRequestDTO request) {
        return postAiVoteService.vote(postId, request.getAi());
    }

    @DeleteMapping("/{postId}/ai-vote")
    public AiVoteResponseDTO removeVote(@PathVariable Long postId) {
        return postAiVoteService.removeVote(postId);
    }

    @GetMapping("/{postId}/ai-vote")
    public AiVoteResponseDTO getVotes(@PathVariable Long postId) {
        return postAiVoteService.getVotes(postId);
    }

    @PutMapping("/{postId}/classification")
    @PreAuthorize("hasRole('MODERATOR')")
    public PostResponseDTO classifyPost(@PathVariable Long postId, @Valid @RequestBody ClassificationRequestDTO request) {
        return postModerationService.classifyPost(postId, request.getClassification());
    }

    @PostMapping("/{postId}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void banPost(@PathVariable Long postId) {
        postModerationService.banPost(postId);
    }

    @DeleteMapping("/{postId}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbanPost(@PathVariable Long postId) {
        postModerationService.unbanPost(postId);
    }

    @GetMapping("/moderation/queue")
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<PostResponseDTO> getModerationQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postModerationService.getModerationQueue(page, size);
    }

    @GetMapping("/moderation/banned")
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<PostResponseDTO> getBannedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postModerationService.getBannedPosts(page, size);
    }

}
