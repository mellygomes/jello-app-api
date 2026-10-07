package com.jello.jello_app.follow.controller;

import com.jello.jello_app.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable Long userId) {
        followService.followUser(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable Long userId) {
        followService.unfollowUser(userId);
    }

}
