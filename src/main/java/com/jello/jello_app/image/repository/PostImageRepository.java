package com.jello.jello_app.image.repository;

import com.jello.jello_app.image.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    // [postId, imageId] sem carregar os bytes da imagem
    @Query("SELECT i.post.id, i.id FROM PostImage i WHERE i.post.id IN :ids ORDER BY i.id")
    List<Object[]> findIdsByPostIds(@Param("ids") Collection<Long> ids);
}
