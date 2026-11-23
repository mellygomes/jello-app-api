package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.enumeration.PostSort;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.image.dto.ImageDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    public static final int MAX_PAGE_SIZE = 50;

    private final PostImageService postImageService;
    private final AuthService authService;
    private final PostRepository postRepository;
    private final TagService tagService;
    private final PostResponseAssembler postResponseAssembler;

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

        List<ImageDTO> savedImages = List.of();
        if (images != null && !images.isEmpty()) {
            savedImages = postImageService.saveImageForPost(images, savedPost);
        }

        PostResponseDTO response = PostMapper.toDto(savedPost);
        response.setImages(savedImages.stream().map(image -> ImageMapper.toImageResponse(image.getId())).toList());
        return response;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    // Post que o usuario pode ver: banido (ou de usuario banido) so aparece pra moderador
    public Post getVisiblePost(Long id, User viewer) {
        Post post = getPostById(id);

        if (!isModerator(viewer) && (post.isBanned() || post.getUser().isBanned())) {
            throw new PostNotFoundException(id);
        }

        return post;
    }

    public PostResponseDTO getPostResponse(Long id) {
        User viewer = authService.getAuthenticatedUser();
        return postResponseAssembler.toResponse(getVisiblePost(id, viewer), viewer);
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .ifPresentOrElse(postRepository::delete, () -> {
                    throw new PostNotFoundException(id);
                });
    }

    @Transactional
    public void addLike(Long postId) {
        User user = authService.getAuthenticatedUser();

        Post post = getVisiblePost(postId, user);
        post.getLikes().add(user);
    }

    @Transactional
    public void removeLike(Long postId, Long userId) {
        Post post = getPostById(postId);

        post.getLikes().removeIf(u -> u.getId().equals(userId));
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

    public PostResponseDTO toResponse(Post post) {
        return postResponseAssembler.toResponse(post, authService.getAuthenticatedUser());
    }

    public Page<PostResponseDTO> findPosts(FilterRequestDTO filter, int page, int size) {
        User user = authService.getAuthenticatedUser();

        Pageable pageable = buildPageable(page, size);

        Specification<Post> spec = Specification
                .where(PostSpecification.withFilter(filter))
                .and(PostSpecification.sortedBy(sortOf(filter)));

        if (!isModerator(user)) {
            spec = spec.and(PostSpecification.onlyVisible());
        }

        return postResponseAssembler.toPage(postRepository.findAll(spec, pageable), user);
    }

    // Explorar: todos os posts visiveis (inclusive de quem eu sigo e os meus), na ordem escolhida
    public Page<PostResponseDTO> findExplorePosts(FilterRequestDTO filter, int page, int size) {
        User user = authService.getAuthenticatedUser();

        Pageable pageable = buildPageable(page, size);

        Specification<Post> spec = Specification
                .where(PostSpecification.withFilter(filter))
                .and(PostSpecification.sortedBy(sortOf(filter)));

        if (!isModerator(user)) {
            spec = spec.and(PostSpecification.onlyVisible());
        }

        return postResponseAssembler.toPage(postRepository.findAll(spec, pageable), user);
    }

    public Page<PostResponseDTO> getAllPostsByUser(Long userId, FilterRequestDTO filter, int page, int size) {
        User viewer = authService.getAuthenticatedUser();

        Pageable pageable = buildPageable(page, size);

        Specification<Post> spec = Specification
                .where(PostSpecification.withFilter(filter))
                .and(PostSpecification.byUserId(userId))
                .and(PostSpecification.sortedBy(sortOf(filter)));

        if (!isModerator(viewer)) {
            spec = spec.and(PostSpecification.onlyVisible());
        }

        return postResponseAssembler.toPage(postRepository.findAll(spec, pageable), viewer);
    }

    public static Pageable buildPageable(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        // sem Sort: a ordem vem do PostSpecification.sortedBy
        return PageRequest.of(Math.max(page, 0), safeSize);
    }

    private static PostSort sortOf(FilterRequestDTO filter) {
        return filter == null || filter.getSort() == null ? PostSort.RECENT : filter.getSort();
    }

    public static boolean isModerator(User user) {
        return user != null && user.hasRole(RoleType.ROLE_MODERATOR.getName());
    }
}
