package com.jello.jello_app.post.repository;

import com.jello.jello_app.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            ORDER BY
                CASE
                    WHEN p.user.id IN :followingIds THEN 0
                    ELSE 1
                END,
                p.createdAt DESC
            """)
    Page<Post> findFeedPosts(
            @Param("followingIds") List<Long> followingIds,
            Pageable pageable
    );

    long countByUserId(Long userId);
}
