package com.jello.jello_app.user.mapper;

import com.jello.jello_app.follow.dto.FollowStatsDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.model.User;

public class UserMapper {
    public static UserDTO toDto(User user) {
        return toDto(user, false);
    }

    public static UserDTO toDto(User user, boolean isFollowing) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .isFollowing(isFollowing)
                .build();
    }

    public static UserDTO toPostAuthorDto(User user, boolean isFollowing) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isFollowing(isFollowing)
                .avatarId(user.getProfilePicture() == null ? null : user.getProfilePicture().getId())
                .build();
    }

    public static ProfileDTO toProfileDto(User user, long postsCount, FollowStatsDTO followStats) {
        return ProfileDTO.builder()
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
                .build();
    }
}
