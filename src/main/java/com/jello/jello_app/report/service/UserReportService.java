package com.jello.jello_app.report.service;

import com.jello.jello_app.report.dto.UpdateUserReportDTO;
import com.jello.jello_app.report.dto.UserReportRequestDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.report.repository.UserReportRepository;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReportService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserReportRepository userReportRepository;

    @Transactional
    public UserReport createUserReport(UserReportRequestDTO request, Long userId) {
        User reported = userService.getUserById(request.getUserReportedId());
        User reporter = userService.getUserById(userId);
        Moderator moderator = userRoleService.getModerator();

        UserReport report = new UserReport();
        report.setContent(request.getContent());
        report.setReportedUser(reported);
        report.setReporterUser(reporter);
        report.setModeratorAnalyst(moderator);

        return userReportRepository.save(report);
    }

    public UserReport getUserReportById(Long reportId) {
        return userReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    public List<UserReportResponseDTO> getAllUsersReports() {
        return userReportRepository.findAll().stream().map(ReportMapper::toUserReportDTO).toList();
    }

    @Transactional
    public UserReportResponseDTO updateUserReport(UpdateUserReportDTO request, Long reportId) {
        UserReport userReport = getUserReportById(reportId);
        userReport.setContent(request.getContent());

        return ReportMapper.toUserReportDTO(userReport);
    }

    @Transactional
    public void deleteUserReport(Long reportId) {
        UserReport report = getUserReportById(reportId);
        userReportRepository.delete(report);
    }
}
