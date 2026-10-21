package com.jello.jello_app.report.repository;

import com.jello.jello_app.report.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
}
