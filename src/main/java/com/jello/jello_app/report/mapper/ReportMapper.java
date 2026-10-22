package com.jello.jello_app.report.mapper;

import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.model.PostReport;

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
}
