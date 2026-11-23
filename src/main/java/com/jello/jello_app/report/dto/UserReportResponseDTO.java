package com.jello.jello_app.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReportResponseDTO {
    private Long id;
    private String content;
    private Long userReporterId;
    private Long userReportedId;
    private Long moderatorAnalystId;
}
