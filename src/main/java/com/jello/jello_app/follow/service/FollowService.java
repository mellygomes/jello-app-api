package com.jello.jello_app.follow.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.follow.exception.FollowAlreadyFollowedException;
import com.jello.jello_app.follow.exception.SelfFollowException;
import com.jello.jello_app.follow.exception.SelfUnfollowException;
import com.jello.jello_app.follow.exception.UnfollowNotFollowedException;
import com.jello.jello_app.follow.model.Follow;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserService userService;
    private final FollowRepository followRepository;
    private final AuthService authService;

    @Transactional
    public void followUser(Long userId) {
        User follower = authService.getAuthenticatedUser();
        User following = userService.getUserById(userId);

        if (follower.getId().equals(userId)) {
            throw new SelfFollowException(userId);
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new FollowAlreadyFollowedException(following.getId());
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setCreatedAt(LocalDateTime.now());

        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(Long userId) {
        User follower = authService.getAuthenticatedUser();
        User following = userService.getUserById(userId);

        if (follower.getId().equals(userId)) {
            throw new SelfUnfollowException(userId);
        }

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new UnfollowNotFollowedException(following.getId()));

        followRepository.delete(follow);
    }
}
