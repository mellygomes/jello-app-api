package com.jello.jello_app.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String bio;
    private Long avatarId;
    // O feed da main usa esse campo no autor do post
    private Boolean isFollowing;
}
