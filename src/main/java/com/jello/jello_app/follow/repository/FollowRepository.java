package com.jello.jello_app.follow.repository;

import com.jello.jello_app.follow.model.Follow;
import com.jello.jello_app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    @Query("""
            SELECT f.following
            FROM Follow f
            WHERE f.follower.id = :userId
            """)
    List<User> findUsersFollowedBy(@Param("userId") Long userId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findFollowingIds(@Param("userId") Long userId);

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId ORDER BY f.createdAt DESC")
    List<User> findFollowersOf(@Param("userId") Long userId);

    long countByFollowerId(Long userId);

    long countByFollowingId(Long userId);
}
