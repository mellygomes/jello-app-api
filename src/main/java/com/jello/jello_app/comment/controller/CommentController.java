package com.jello.jello_app.comment.controller;

import com.jello.jello_app.comment.dto.AddCommentRequestDTO;
import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.mapper.CommentMapper;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/add")
    public CommentResponseDTO addComment(@PathVariable Long postId, @RequestBody AddCommentRequestDTO request) {
        Comment newComment = commentService.addComment(request.getComment(), postId);
        return CommentMapper.toDto(newComment);
    }

    @GetMapping("/{postId}/all")
    public List<CommentResponseDTO> getAllCommentsFromPost(@PathVariable Long postId) {
        return commentService.getAllCommentsFromPost(postId);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("@securityUtils.canDeleteComment(#commentId, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
