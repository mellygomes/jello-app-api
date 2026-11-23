package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.enumeration.PostClassification;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostModerationService {

    private final PostService postService;
    private final PostRepository postRepository;
    private final AuthService authService;
    private final UserRoleService userRoleService;
    private final PostResponseAssembler postResponseAssembler;

    // Moderador define de vez se o post e IA, arte humana ou volta pra sem classificacao
    @Transactional
    public PostResponseDTO classifyPost(Long postId, PostClassification classification) {
        User actor = authService.getAuthenticatedUser();
        Post post = postService.getPostById(postId);

        post.setAiClassified(classification == PostClassification.AI);
        post.setHumanClassified(classification == PostClassification.HUMAN);

        if (classification == PostClassification.NONE) {
            post.setModerator(null);
        } else {
            Moderator moderator = userRoleService.getModeratorByUser(actor);
            post.setModerator(moderator);
        }

        Post saved = postRepository.save(post);
        return postResponseAssembler.toResponse(saved, actor);
    }

    @Transactional
    public void banPost(Long postId) {
        Post post = postService.getPostById(postId);
        post.setBanned(true);
        postRepository.save(post);
    }

    @Transactional
    public void unbanPost(Long postId) {
        Post post = postService.getPostById(postId);
        post.setBanned(false);
        postRepository.save(post);
    }

    public Page<PostResponseDTO> getModerationQueue(int page, int size) {
        User actor = authService.getAuthenticatedUser();
        return postResponseAssembler.toPage(postRepository.findModerationQueue(plainPageable(page, size)), actor);
    }

    public Page<PostResponseDTO> getBannedPosts(int page, int size) {
        User actor = authService.getAuthenticatedUser();
        Pageable pageable = PostService.buildPageable(page, size);
        return postResponseAssembler.toPage(postRepository.findByBannedTrueOrderByCreatedAtDesc(pageable), actor);
    }

    // A ordenacao da fila ja vem da query
    private Pageable plainPageable(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), PostService.MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize);
    }
}
