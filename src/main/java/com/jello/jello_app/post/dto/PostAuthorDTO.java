package com.jello.jello_app.post.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostAuthorDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Long avatarId;
    private boolean following;
    private boolean moderator;
}
