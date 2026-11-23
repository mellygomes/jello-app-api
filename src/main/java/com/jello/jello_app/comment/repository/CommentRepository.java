package com.jello.jello_app.comment.repository;

import com.jello.jello_app.comment.model.Comment;
import com.jello.jello_app.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post postId);

    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post.id IN :ids GROUP BY c.post.id")
    List<Object[]> countByPostIds(@Param("ids") Collection<Long> ids);
}
