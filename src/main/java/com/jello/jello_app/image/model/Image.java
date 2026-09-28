package com.jello.jello_app.image.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jello.jello_app.common.model.Auditable;
import com.jello.jello_app.post.model.Post;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_image_post")
// TODO: Renomear para PostImage
public class Image extends Auditable {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
