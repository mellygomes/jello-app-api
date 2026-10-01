package com.jello.jello_app.follow.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowStats {
    private long followersCount;
    private long followingCount;
}
