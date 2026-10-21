package com.jello.jello_app.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostReportRequestDTO {
    private Long postId;
    private Long userId;
    private Long moderatorId;
}
