package com.jello.jello_app.user.dto;

import com.jello.jello_app.image.dto.AvatarDTO;
import com.jello.jello_app.image.dto.CoverDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String bio;
    private AvatarDTO avatar;
    private CoverDTO cover;
    private long posts;
    private long followers;
    private long following;
    // true se quem ta vendo ja segue esse perfil
    private boolean followedByMe;
    private boolean moderator;
    private boolean banned;
}
