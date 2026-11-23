package com.jello.jello_app.report.controller;

import com.jello.jello_app.report.dto.PostReportInfoDTO;
import com.jello.jello_app.report.dto.PostReportRequestDTO;
import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UpdatePostReportDTO;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.service.PostReportService;
import com.jello.jello_app.security.user.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/posts/reports")
public class PostReportController {

    private final PostReportService postReportService;

    // Qualquer usuario logado pode denunciar, o resto e so pra moderador
    @PostMapping("/create")
    public PostReportResponseDTO createReport(
            @Valid @RequestBody PostReportRequestDTO request,
            @AuthenticationPrincipal AppUserDetails details
            ) {
        PostReport report = postReportService.createPostReport(request, details.getId());
        return ReportMapper.toPostReportDTO(report);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public PostReportResponseDTO getReportById(@PathVariable Long reportId) {
        PostReport report = postReportService.getPostReportById(reportId);
        return ReportMapper.toPostReportDTO(report);
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public List<PostReportResponseDTO> getAllReports() {
        return postReportService.getAllPostReports();
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('MODERATOR')")
    public List<PostReportInfoDTO> getReports() {
        return postReportService.getReports();
    }

    @PutMapping("/approve/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public void approve(@PathVariable Long reportId) {
        postReportService.approveReport(reportId);
    }

    @PutMapping("/unapprove/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public void unapprove(@PathVariable Long reportId) {
        postReportService.unapproveReport(reportId);
    }

    @PutMapping("/update/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public PostReportResponseDTO updateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdatePostReportDTO request
    ) {
        return postReportService.updatePostReport(request, reportId);
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long reportId) {
        postReportService.deletePostReport(reportId);
    }
}
