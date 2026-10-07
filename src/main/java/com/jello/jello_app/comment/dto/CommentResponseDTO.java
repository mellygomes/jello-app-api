package com.jello.jello_app.comment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponseDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String user;
    private String content;
}
