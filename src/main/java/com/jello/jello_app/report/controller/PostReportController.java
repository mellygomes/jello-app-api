package com.jello.jello_app.report.controller;

import com.jello.jello_app.report.dto.PostReportRequestDTO;
import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UpdatePostReportDTO;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.service.PostReportService;
import com.jello.jello_app.security.user.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/posts/reports")
public class PostReportController {

    private final PostReportService postReportService;

    @PostMapping("/create")
    public PostReportResponseDTO createReport(
            @RequestBody PostReportRequestDTO request,
            @AuthenticationPrincipal AppUserDetails details
            ) {
        PostReport report = postReportService.createPostReport(request, details.getId());
        return ReportMapper.toPostReportDTO(report);
    }

    @GetMapping("/{reportId}")
    public PostReportResponseDTO getReportById(@PathVariable Long reportId) {
        PostReport report = postReportService.getPostReportById(reportId);
        return ReportMapper.toPostReportDTO(report);
    }

    @GetMapping("/list")
    public List<PostReportResponseDTO> getAllReports() {
        return postReportService.getAllPostReports();
    }

    @PutMapping("/update/{reportId}")
    public PostReportResponseDTO updateReport(
            @PathVariable Long reportId,
            @RequestBody UpdatePostReportDTO request
    ) {
        return postReportService.updatePostReport(request, reportId);
    }

    @DeleteMapping("/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long reportId) {
        postReportService.deletePostReport(reportId);
    }
}
