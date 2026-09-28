package com.jello.jello_app.post.service;

import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.post.dto.CreatePostRequest;
import com.jello.jello_app.post.dto.PostDTO;
import com.jello.jello_app.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    Post createPost(CreatePostRequest request, List<MultipartFile> images);
    Post getPostById(Long id);
    void deletePost(Long id);
    Post updatePost(CreatePostRequest request, Long postId);
    Page<PostDTO> getFeedPosts(int page, int size);
}
