package com.jello.jello_app.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Long avatarId;
    private boolean following;
    private boolean banned;
    private boolean moderator;
}
