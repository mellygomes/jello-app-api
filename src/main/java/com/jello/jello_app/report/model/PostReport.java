package com.jello.jello_app.report.model;

import com.jello.jello_app.common.model.Auditable;
import com.jello.jello_app.moderator.model.Moderator;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_report_post")
public class PostReport extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_post_id", nullable = false)
    private Post reportedPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporterUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moderator_analyst_id", nullable = false)
    private Moderator moderatorAnalyst;

    @Column(name = "is_approved", nullable = false)
    private boolean isApproved;
}
