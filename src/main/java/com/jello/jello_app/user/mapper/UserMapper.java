package com.jello.jello_app.user.mapper;

import com.jello.jello_app.follow.dto.FollowStats;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UserDTO;
import com.jello.jello_app.user.model.User;

public class UserMapper {
    public static UserDTO toDto(User user) {
        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .build();
    }

    public static ProfileDTO toProfileDto(User user, long postsCount, FollowStats followStats) {
        return ProfileDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatar(user.getProfilePicture())
                .cover(user.getProfileCover())
                .followers(followStats.getFollowersCount())
                .following(followStats.getFollowingCount())
                .posts(postsCount)
                .build();
    }
}
