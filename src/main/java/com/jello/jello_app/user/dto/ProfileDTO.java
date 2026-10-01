package com.jello.jello_app.user.dto;

import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.model.UserCover;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String bio;
    private UserAvatar avatar;
    private UserCover cover;
    private long posts;
    private long followers;
    private long following;
}
