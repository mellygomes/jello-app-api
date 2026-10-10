package com.jello.jello_app.comment.mapper;

import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.model.Comment;

public class CommentMapper {
    public static CommentResponseDTO toDto(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .user(comment.getUser().getUsername())
                .content(comment.getContent())
                .build();
    }
}
