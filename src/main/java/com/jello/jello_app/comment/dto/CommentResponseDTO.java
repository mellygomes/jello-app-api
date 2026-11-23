package com.jello.jello_app.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CommentResponseDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String user;
    private Long userAvatarId;
    private boolean userModerator;
    private String content;
    private Long parentId;
    private OffsetDateTime createdAt;
}
