package com.jello.jello_app.user.mapper;

import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.follow.dto.FollowStatsDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.dto.UserSummaryDTO;
import com.jello.jello_app.user.model.User;

public class UserMapper {
    public static UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatarId(avatarId(user))
                .build();
    }

    // Versao sem e-mail, pra usar em respostas que outros usuarios podem ver
    public static UserDTO toPublicDto(User user) {
        UserDTO dto = toDto(user);
        dto.setEmail(null);
        return dto;
    }

    // Igual ao publico, mas dizendo se quem ta vendo ja segue esse usuario
    public static UserDTO toPublicDto(User user, boolean following) {
        UserDTO dto = toPublicDto(user);
        dto.setIsFollowing(following);
        return dto;
    }

    public static UserSummaryDTO toSummaryDto(User user, boolean following) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarId(avatarId(user))
                .following(following)
                .banned(user.isBanned())
                .moderator(user.hasRole(RoleType.ROLE_MODERATOR.getName()))
                .build();
    }

    public static ProfileDTO toProfileDto(User user, long postsCount, FollowStatsDTO followStats) {
        return ProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatar(ImageMapper.toAvatarDTO(user.getProfilePicture()))
                .cover(ImageMapper.toCoverDTO(user.getProfileCover()))
                .followers(followStats.getFollowersCount())
                .following(followStats.getFollowingCount())
                .posts(postsCount)
                .moderator(user.hasRole(RoleType.ROLE_MODERATOR.getName()))
                .banned(user.isBanned())
                .build();
    }

    public static Long avatarId(User user) {
        return user.getProfilePicture() != null ? user.getProfilePicture().getId() : null;
    }
}
