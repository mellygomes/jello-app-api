package com.jello.jello_app.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostReportResponseDTO {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private Long moderatorId;
}
