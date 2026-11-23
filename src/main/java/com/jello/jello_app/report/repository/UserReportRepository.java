package com.jello.jello_app.report.repository;

import com.jello.jello_app.report.model.UserReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    @EntityGraph(attributePaths = {"reportedUser", "reporterUser"})
    @Query("SELECT ur FROM UserReport ur")
    List<UserReport> findAllWithDetails();
}
