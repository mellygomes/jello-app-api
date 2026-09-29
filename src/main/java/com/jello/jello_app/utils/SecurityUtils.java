package com.jello.jello_app.utils;

import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.repository.CommentRepository;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Boolean canDeleteComment(Long commentId, Authentication authentication) {
        String username = authentication.getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found!"));

        Boolean isModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), RoleType.ROLE_MODERATOR.getName()));

        Boolean isCommentOwner = comment.getUser().getUsername().equals(username);

        Boolean isPostOwner = comment.getPost().getUser().getUsername().equals(username);

        return isModerator || isCommentOwner || isPostOwner;
    }

    public Boolean canModifyPost(Long postId, Authentication authentication) {
        String username = authentication.getName();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found!"));

        Boolean isModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), RoleType.ROLE_MODERATOR.getName()));

        Boolean isPostOwner = post.getUser().getUsername().equals(username);

        return isModerator || isPostOwner;
    }
}
