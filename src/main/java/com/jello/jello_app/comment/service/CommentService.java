package com.jello.jello_app.comment.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.exception.CommentNotFoundException;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.repository.CommentRepository;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final AuthService authService;

    public Comment addComment(String content, Long postId) {
        User user = authService.getAuthenticatedUser();
        Post post = postService.getPostById(postId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(user);

        return commentRepository.save(comment);
    }

    public List<CommentResponseDTO> getAllCommentsFromPost(Long postId) {
        Post post = postService.getPostById(postId);
        List<Comment> comments = commentRepository.findByPost(post);

        return comments.stream()
                .map(com -> CommentResponseDTO.builder()
                        .id(com.getId())
                        .postId(com.getPost().getId())
                        .user(com.getUser().getUsername())
                        .content(com.getContent())
                        .build())
                .toList();
    }

    public void deleteComment(Long commentId) {
        commentRepository.findById(commentId)
                .ifPresentOrElse(commentRepository::delete, () -> {
                    throw new CommentNotFoundException(commentId);
                });
    }
}
