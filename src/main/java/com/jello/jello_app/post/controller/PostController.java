package com.jello.jello_app.post.controller;

import com.jello.jello_app.post.dto.CreatePostRequestDTO;
import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.dto.UpdatePostRequestDTO;
import com.jello.jello_app.post.mapper.PostMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/posts")
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public PostResponseDTO createPost(@RequestPart(value = "images") List<MultipartFile> images,
                                      @RequestPart("post") String postRequest) {
        ObjectMapper mapper = new ObjectMapper();
        CreatePostRequestDTO request = mapper.readValue(postRequest, CreatePostRequestDTO.class);
        return postService.createPost(request, images);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("@securityUtils.canModifyPost(#postId, authentication)")
    public PostResponseDTO updatePost(@PathVariable Long postId, @RequestBody UpdatePostRequestDTO request) {
        Post post = postService.updatePost(request, postId);
        return PostMapper.toDto(post);
    }

    @GetMapping("/list")
    public Page<PostResponseDTO> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute FilterRequestDTO request
    ) {
        return postService.findPosts(request, page, size);
    }

}
