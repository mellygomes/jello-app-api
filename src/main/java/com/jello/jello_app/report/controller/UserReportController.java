package com.jello.jello_app.report.controller;

import com.jello.jello_app.report.dto.UpdateUserReportDTO;
import com.jello.jello_app.report.dto.UserReportInfoDTO;
import com.jello.jello_app.report.dto.UserReportRequestDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.report.service.UserReportService;
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
@RequestMapping("${api.prefix}/users/reports")
public class UserReportController {

    private final UserReportService userReportService;

    // Qualquer usuario logado pode denunciar, o resto e so pra moderador
    @PostMapping("/create")
    public UserReportResponseDTO createReport(
            @Valid @RequestBody UserReportRequestDTO request,
            @AuthenticationPrincipal AppUserDetails details
    ) {
        UserReport report = userReportService.createUserReport(request, details.getId());
        return ReportMapper.toUserReportDTO(report);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public UserReportResponseDTO getReportById(@PathVariable Long reportId) {
        UserReport report = userReportService.getUserReportById(reportId);
        return ReportMapper.toUserReportDTO(report);
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public List<UserReportResponseDTO> getAllReports() {
        return userReportService.getAllUsersReports();
    }

    @PutMapping("/update/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public UserReportResponseDTO updateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateUserReportDTO request
    ) {
        return userReportService.updateUserReport(request, reportId);
    }

    @DeleteMapping("{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long reportId) {
        userReportService.deleteUserReport(reportId);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('MODERATOR')")
    public List<UserReportInfoDTO> getReports() {
        return userReportService.getReports();
    }

    @PutMapping("/approve/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public void approveReport(@PathVariable Long reportId) {
        userReportService.approveReport(reportId);
    }

    @PutMapping("/unapprove/{reportId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public void unapproveReport(@PathVariable Long reportId) {
        userReportService.unapproveReport(reportId);
    }
}
