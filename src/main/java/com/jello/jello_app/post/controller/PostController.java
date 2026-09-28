package com.jello.jello_app.post.controller;

import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.post.dto.CreatePostRequest;
import com.jello.jello_app.post.dto.PostDTO;
import com.jello.jello_app.post.mapper.PostMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/posts")
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> createPost(@RequestPart(value = "images") List<MultipartFile> images,
                                                  @RequestPart("post") String postRequest) {
        ObjectMapper mapper = new ObjectMapper();
        CreatePostRequest request = mapper.readValue(postRequest, CreatePostRequest.class);

        try {
            Post createdPost = postService.createPost(request, images);
            PostDTO post = PostMapper.toDto(createdPost);
            return ResponseEntity.ok(new ApiResponse("Post criado com sucesso!", post));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return ResponseEntity.ok(new ApiResponse("Post deletado com sucesso!", null));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    public ResponseEntity<ApiResponse> updatePost(@PathVariable Long postId, @RequestBody CreatePostRequest request) {
        try {
            Post post = postService.updatePost(request, postId);
            PostDTO postResponse = PostMapper.toDto(post);
            return ResponseEntity.ok(new ApiResponse("Post atualizado com sucesso!", postResponse));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }

    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<PostDTO> posts = postService.getFeedPosts(page, size);
            return ResponseEntity.ok(new ApiResponse("Posts carregados com sucesso!", posts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
