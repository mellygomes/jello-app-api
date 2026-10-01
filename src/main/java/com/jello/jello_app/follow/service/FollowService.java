package com.jello.jello_app.follow.service;

public interface FollowService {
    void followUser(Long userId);

    void unfollowUser(Long userId);

}
