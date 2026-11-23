package com.jello.jello_app.report.service;

import com.jello.jello_app.report.dto.UpdateUserReportDTO;
import com.jello.jello_app.report.dto.UserReportInfoDTO;
import com.jello.jello_app.report.dto.UserReportRequestDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.exception.ReportAlreadyApprovedException;
import com.jello.jello_app.report.exception.ReportAlreadyUnapprovedException;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.exception.SelfReportException;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.report.repository.UserReportRepository;
import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.mapper.UserMapper;
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
    private final AuthService authService;
    private final UserReportRepository userReportRepository;

    @Transactional
    public UserReport createUserReport(UserReportRequestDTO request, Long userId) {
        User reported = userService.getUserById(request.getUserReportedId());
        User reporter = userService.getUserById(userId);
        // sem moderador cadastrado a denuncia nasce sem analista, nao deixa de ser criada
        Moderator moderator = userRoleService.findModerator().orElse(null);

        if (reported == reporter) {
            throw new SelfReportException(userId);
        }

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

    public List<UserReportInfoDTO> getReports() {
        return userReportRepository.findAllWithDetails().stream()
                .map(ur -> UserReportInfoDTO.builder()
                        .id(ur.getId())
                        .content(ur.getContent())
                        .isApproved(ur.isApproved())
                        .userReporter(ur.getReporterUser().getFirstName())
                        .reportedId(ur.getReportedUser().getId())
                        .userReported(UserMapper.toDto(ur.getReportedUser()))
                        .reportedBanned(ur.getReportedUser().isBanned())
                        .build()
                )
                .toList();
    }

    @Transactional
    public void approveReport(Long reportId) {
        UserReport report = getUserReportById(reportId);

        if (report.isApproved()) {
            throw new ReportAlreadyApprovedException(reportId);
        }

        claimIfUnassigned(report);
        report.setApproved(true);
    }

    @Transactional
    public void unapproveReport(Long reportId) {
        UserReport report = getUserReportById(reportId);

        if (!report.isApproved()) {
            throw new ReportAlreadyUnapprovedException(reportId);
        }

        claimIfUnassigned(report);
        report.setApproved(false);
    }

    // Quem analisa assume a denuncia se ela ficou sem analista (ex: o moderador anterior foi apagado)
    private void claimIfUnassigned(UserReport report) {
        if (report.getModeratorAnalyst() != null) return;

        User actor = authService.getAuthenticatedUser();
        if (actor != null) {
            report.setModeratorAnalyst(userRoleService.getModeratorByUser(actor));
        }
    }
}
