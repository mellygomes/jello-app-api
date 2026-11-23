package com.jello.jello_app.post.repository;

import com.jello.jello_app.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    long countByUserIdAndBannedFalse(Long userId);

    Page<Post> findByBannedTrueOrderByCreatedAtDesc(Pageable pageable);

    // Fila do moderador: posts com voto de IA que ainda nao foram classificados, mais votados primeiro
    @Query(value = """
            SELECT p
            FROM Post p
            JOIN PostAiVote v ON v.post = p
            WHERE v.voteAi = true
              AND p.banned = false
              AND p.aiClassified = false
              AND p.humanClassified = false
              AND p.user.banned = false
            GROUP BY p
            ORDER BY COUNT(v) DESC, p.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p)
            FROM Post p
            JOIN PostAiVote v ON v.post = p
            WHERE v.voteAi = true
              AND p.banned = false
              AND p.aiClassified = false
              AND p.humanClassified = false
              AND p.user.banned = false
            """)
    Page<Post> findModerationQueue(Pageable pageable);

    @Query("SELECT p.id, COUNT(u) FROM Post p JOIN p.likes u WHERE p.id IN :ids GROUP BY p.id")
    List<Object[]> countLikesByPostIds(@Param("ids") Collection<Long> ids);

    @Query("SELECT p.id FROM Post p JOIN p.likes u WHERE p.id IN :ids AND u.id = :userId")
    List<Long> findLikedPostIds(@Param("ids") Collection<Long> ids, @Param("userId") Long userId);
}
