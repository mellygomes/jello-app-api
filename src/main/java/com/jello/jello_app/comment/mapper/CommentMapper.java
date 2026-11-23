package com.jello.jello_app.comment.mapper;

import com.jello.jello_app.comment.dto.CommentResponseDTO;
import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.user.mapper.UserMapper;

import java.time.ZoneId;
import java.time.ZoneOffset;

public class CommentMapper {
    public static CommentResponseDTO toDto(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .user(comment.getUser().getUsername())
                .userAvatarId(UserMapper.avatarId(comment.getUser()))
                .userModerator(comment.getUser().hasRole(RoleType.ROLE_MODERATOR.getName()))
                .content(comment.getContent())
                .parentId(comment.getOriginalCommentId())
                .createdAt(comment.getCreatedAt() != null
                        ? comment.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime()
                        : null)
                .build();
    }
}
