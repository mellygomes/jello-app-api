package com.jello.jello_app.report.dto;

import com.jello.jello_app.image.dto.ImageResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostReportInfoDTO {
    private Long id;
    private String reportContent;
    private boolean isApproved;
    private String post;
    private String name;
    private String postContent;
    private List<ImageResponseDTO> images;
}
