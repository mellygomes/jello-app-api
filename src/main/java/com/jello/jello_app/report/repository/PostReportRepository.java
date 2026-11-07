package com.jello.jello_app.report.repository;

import com.jello.jello_app.report.model.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
}
