package com.jello.jello_app.post.repository;

import com.jello.jello_app.post.model.PostAiVote;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostAiVoteRepository extends JpaRepository<PostAiVote, Long> {

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM PostAiVote pv
            WHERE pv.user.id = :userId AND pv.post.id = :postId
            """)
    int deleteByUserIdAndPostId(Long userId, Long postId);

    Optional<PostAiVote> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostIdAndVoteAi(Long postId, boolean voteAi);

    // [postId, voteAi, total]
    @Query("""
            SELECT v.post.id, v.voteAi, COUNT(v)
            FROM PostAiVote v
            WHERE v.post.id IN :ids
            GROUP BY v.post.id, v.voteAi
            """)
    List<Object[]> countByPostIds(@Param("ids") Collection<Long> ids);

    // [postId, voteAi] dos votos de um usuario
    @Query("SELECT v.post.id, v.voteAi FROM PostAiVote v WHERE v.user.id = :userId AND v.post.id IN :ids")
    List<Object[]> findUserVotes(@Param("ids") Collection<Long> ids, @Param("userId") Long userId);
}
