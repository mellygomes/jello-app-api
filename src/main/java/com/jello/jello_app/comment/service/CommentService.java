package com.jello.jello_app.comment.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.exception.CommentNotFoundException;
import com.jello.jello_app.comment.mapper.CommentMapper;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.repository.CommentRepository;
import com.jello.jello_app.common.exception.BusinessException;
import com.jello.jello_app.post.exception.PostNotFoundException;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final AuthService authService;

    public Comment addComment(String content, Long postId) {
        return addComment(content, postId, null);
    }

    public Comment addComment(String content, Long postId, Long parentId) {
        User user = authService.getAuthenticatedUser();
        Post post = postService.getPostById(postId);
        assertVisible(post, user);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(user);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException(parentId));

            if (!parent.getPost().getId().equals(post.getId())) {
                throw new BusinessException("O comentário respondido não pertence a essa postagem.");
            }

            // resposta de resposta fica ligada ao comentario de cima (so um nivel)
            comment.setOriginalCommentId(parent.getOriginalCommentId() != null ? parent.getOriginalCommentId() : parent.getId());
        }

        return commentRepository.save(comment);
    }

    public List<CommentResponseDTO> getAllCommentsFromPost(Long postId) {
        Post post = postService.getPostById(postId);
        assertVisible(post, authService.getAuthenticatedUser());

        List<Comment> comments = commentRepository.findByPost(post);

        return comments.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(CommentMapper::toDto)
                .toList();
    }

    public void deleteComment(Long commentId) {
        commentRepository.findById(commentId)
                .ifPresentOrElse(commentRepository::delete, () -> {
                    throw new CommentNotFoundException(commentId);
                });
    }

    // Post banido (ou de usuario banido) so deixa moderador mexer nos comentarios
    private void assertVisible(Post post, User viewer) {
        boolean hidden = post.isBanned() || (post.getUser() != null && post.getUser().isBanned());

        if (hidden && !PostService.isModerator(viewer)) {
            throw new PostNotFoundException(post.getId());
        }
    }
}
