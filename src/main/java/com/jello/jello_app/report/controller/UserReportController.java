package com.jello.jello_app.report.controller;

import com.jello.jello_app.report.dto.UpdateUserReportDTO;
import com.jello.jello_app.report.dto.UserReportRequestDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.report.service.UserReportService;
import com.jello.jello_app.security.user.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users/reports")
public class UserReportController {

    private final UserReportService userReportService;

    @PostMapping("/create")
    public UserReportResponseDTO createReport(
            @RequestBody UserReportRequestDTO request,
            @AuthenticationPrincipal AppUserDetails details
    ) {
        UserReport report = userReportService.createUserReport(request, details.getId());
        return ReportMapper.toUserReportDTO(report);
    }

    @GetMapping("/{reportId}")
    public UserReportResponseDTO getReportById(@PathVariable Long reportId) {
        UserReport report = userReportService.getUserReportById(reportId);
        return ReportMapper.toUserReportDTO(report);
    }

    @GetMapping("/list")
    public List<UserReportResponseDTO> getAllReports() {
        return userReportService.getAllUsersReports();
    }

    @PutMapping("/update/{reportId}")
    public UserReportResponseDTO updateReport(
            @PathVariable Long reportId,
            @RequestBody UpdateUserReportDTO request
    ) {
        return userReportService.updateUserReport(request, reportId);
    }

    @DeleteMapping("{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long reportId) {
        userReportService.deleteUserReport(reportId);
    }

}
