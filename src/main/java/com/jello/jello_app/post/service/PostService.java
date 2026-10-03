package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.service.PostImageService;
import com.jello.jello_app.post.dto.CreatePostRequest;
import com.jello.jello_app.post.dto.PostDTO;
import com.jello.jello_app.post.mapper.PostMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostImageService postImageService;
    private final AuthService authService;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    @Transactional
    public Post createPost(CreatePostRequest request, List<MultipartFile> images) {
        try {
            User user = authService.getAuthenticatedUser();

            Post post = new Post();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setUser(user);

            Post savedPost = postRepository.save(post);

            if (images != null && !images.isEmpty()) {
                postImageService.saveImageForPost(images, savedPost);
            }

            return savedPost;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado!"));
    }

    public long getCountPostsByUserId(Long userId) {
        return postRepository.countByUserId(userId);
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .ifPresentOrElse(postRepository::delete, () -> {
                    throw new RuntimeException("Falha ao deletar Post. Post não encontrado!");
                });
    }

    public Post updatePost(CreatePostRequest request, Long postId) {
        return postRepository.findById(postId)
                .map(existingPost -> {
                    existingPost.setTitle(request.getTitle());
                    existingPost.setContent(request.getContent());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new RuntimeException("Falha ao atualizar o Post. Post não encontrado!"));
    }

    public Page<PostDTO> getFeedPosts(int page, int size) {
        User user = authService.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        List<Long> followingIds = followRepository.findUsersFollowedBy(user.getId())
                .stream()
                .map(User::getId)
                .toList();

        Page<Post> posts;

        if (followingIds.isEmpty()) {
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = postRepository.findFeedPosts(followingIds, pageable);
        }

        return posts.map(PostMapper::toDto);
    }
}
