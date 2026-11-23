package com.jello.jello_app.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserReportDTO {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 1000)
    private String content;
}
