package com.jello.jello_app.report.mapper;

import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.model.UserReport;

public class ReportMapper {
    public static PostReportResponseDTO toPostReportDTO(PostReport postReport) {
        return PostReportResponseDTO.builder()
                .id(postReport.getId())
                .content(postReport.getContent())
                .postReportedId(postReport.getReportedPost().getId())
                .userReporterId(postReport.getReporterUser().getId())
                .moderatorAnalystId(postReport.getModeratorAnalyst().getId())
                .build();
    }
    public static UserReportResponseDTO toUserReportDTO(UserReport userReport) {
        return UserReportResponseDTO.builder()
                .id(userReport.getId())
                .content(userReport.getContent())
                .userReporterId(userReport.getReporterUser().getId())
                .userReportedId(userReport.getReportedUser().getId())
                .moderatorAnalystId(userReport.getModeratorAnalyst().getId())
                .build();
    }
}
