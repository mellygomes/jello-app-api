package com.jello.jello_app.report.repository;

import com.jello.jello_app.report.model.PostReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    @EntityGraph(attributePaths = {"reporterUser", "reportedPost", "reportedPost.images"})
    @Query("SELECT pr FROM PostReport pr")
    List<PostReport> findAllWithDetails();
}
