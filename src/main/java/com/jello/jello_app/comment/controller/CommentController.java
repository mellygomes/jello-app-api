package com.jello.jello_app.comment.controller;

import com.jello.jello_app.comment.dto.AddCommentRequest;
import com.jello.jello_app.comment.dto.CommentDTO;
import com.jello.jello_app.comment.mapper.CommentMapper;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.comment.service.CommentService;
import com.jello.jello_app.common.dto.ApiResponse;
import com.jello.jello_app.post.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/add")
    public ResponseEntity<ApiResponse> addComment(@PathVariable Long postId, @RequestBody AddCommentRequest request) {
        try {
            Comment newComment = commentService.addComment(request.getComment(), postId);
            CommentDTO commentResponse = CommentMapper.toDto(newComment);
            return ResponseEntity.ok(new ApiResponse("Comment added!", commentResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{postId}/all")
    public ResponseEntity<ApiResponse> getAllCommentsFromPost(@PathVariable Post post) {
        List<CommentDTO> comments = commentService.getAllCommentsFromPost(post);
        return ResponseEntity.ok(new ApiResponse("Comments listed!", comments));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("@securityUtils.canDeleteComment(#commentId, authentication)")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(new ApiResponse("Comment deleted!", commentId));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
