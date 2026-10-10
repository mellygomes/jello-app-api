package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.service.PostImageService;
import com.jello.jello_app.post.dto.CreatePostRequestDTO;
import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.dto.UpdatePostRequestDTO;
import com.jello.jello_app.post.exception.PostNotFoundException;
import com.jello.jello_app.post.mapper.PostMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.post.repository.PostSpecification;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.tag.service.TagService;
import com.jello.jello_app.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostImageService postImageService;
    private final AuthService authService;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final TagService tagService;

    @Transactional
    public PostResponseDTO createPost(CreatePostRequestDTO request, List<MultipartFile> images) {
        User user = authService.getAuthenticatedUser();

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUser(user);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagService.getAllTagsById(request.getTagIds());
            post.setTags(new HashSet<>(tags));
        }

        Post savedPost = postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            postImageService.saveImageForPost(images, savedPost);
        }

        return PostMapper.toDto(savedPost);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .ifPresentOrElse(postRepository::delete, () -> {
                    throw new PostNotFoundException(id);
                });
    }

    public Post updatePost(UpdatePostRequestDTO request, Long postId) {
        return postRepository.findById(postId)
                .map(existingPost -> {
                    existingPost.setTitle(request.getTitle());
                    existingPost.setContent(request.getContent());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    public Page<PostResponseDTO> findPosts(FilterRequestDTO filter, int page, int size) {
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

        Specification<Post> spec = Specification
                .where(PostSpecification.withFilter(filter))
                .and(PostSpecification.orderByFollowingUser(followingIds));

        return postRepository.findAll(spec, pageable).map(PostMapper::toDto);
    }
}
