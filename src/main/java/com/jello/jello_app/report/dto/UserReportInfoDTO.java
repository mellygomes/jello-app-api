package com.jello.jello_app.report.dto;

import com.jello.jello_app.user.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReportInfoDTO {
    private Long id;
    private String content;
    private boolean isApproved;
    private String userReporter;
    private UserDTO userReported;
}
