package com.jello.jello_app.report.service;

import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.report.dto.PostReportInfoDTO;
import com.jello.jello_app.report.dto.PostReportRequestDTO;
import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UpdatePostReportDTO;
import com.jello.jello_app.report.exception.ReportAlreadyApprovedException;
import com.jello.jello_app.report.exception.ReportAlreadyUnapprovedException;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.repository.PostReportRepository;
import com.jello.jello_app.auth.service.AuthService;
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
public class PostReportService {

    private final UserService userService;
    private final PostService postService;
    private final PostReportRepository postReportRepository;
    private final UserRoleService userRoleService;
    private final AuthService authService;

    @Transactional
    public PostReport createPostReport(PostReportRequestDTO request, Long userId) {
        User user = userService.getUserById(userId);
        Post post = postService.getPostById(request.getPostId());
        // sem moderador cadastrado a denuncia nasce sem analista, nao deixa de ser criada
        Moderator moderator = userRoleService.findModerator().orElse(null);

        PostReport postReport = new PostReport();
        postReport.setReporterUser(user);
        postReport.setReportedPost(post);
        postReport.setModeratorAnalyst(moderator);
        postReport.setContent(request.getContent());

        return postReportRepository.save(postReport);
    }

    public PostReport getPostReportById(Long reportId) {
        return postReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    public List<PostReportResponseDTO> getAllPostReports() {
        return postReportRepository.findAll().stream().map(ReportMapper::toPostReportDTO).toList();
    }

    @Transactional
    public PostReportResponseDTO updatePostReport(UpdatePostReportDTO request, Long reportId) {
        PostReport postReport = getPostReportById(reportId);
        postReport.setContent(request.getContent());

        return ReportMapper.toPostReportDTO(postReport);
    }

    @Transactional
    public void deletePostReport(Long reportId) {
        PostReport postReport = getPostReportById(reportId);
        postReportRepository.delete(postReport);
    }

    public List<PostReportInfoDTO> getReports() {
        return postReportRepository.findAllWithDetails().stream()
                .map(pr -> PostReportInfoDTO.builder()
                        .id(pr.getId())
                        .reportContent(pr.getContent())
                        .isApproved(pr.isApproved())
                        .post(pr.getReportedPost().getTitle())
                        .name(pr.getReporterUser().getFirstName() + " " + pr.getReporterUser().getLastName())
                        .postContent(pr.getReportedPost().getContent())
                        .postId(pr.getReportedPost().getId())
                        .postAuthorId(pr.getReportedPost().getUser().getId())
                        .postBanned(pr.getReportedPost().isBanned())
                        .images(pr.getReportedPost().getImages().stream().distinct().map(ImageMapper::toTeste).toList())
                        .build()
                )
                .toList();
    }

    @Transactional
    public void approveReport(Long reportId) {
        PostReport report = getPostReportById(reportId);

        if (report.isApproved()) {
            throw new ReportAlreadyApprovedException(reportId);
        }

        claimIfUnassigned(report);
        report.setApproved(true);
    }

    @Transactional
    public void unapproveReport(Long reportId) {
        PostReport report = getPostReportById(reportId);

        if (!report.isApproved()) {
            throw new ReportAlreadyUnapprovedException(reportId);
        }

        claimIfUnassigned(report);
        report.setApproved(false);
    }

    // Quem analisa assume a denuncia se ela ficou sem analista (ex: o moderador anterior foi apagado)
    private void claimIfUnassigned(PostReport report) {
        if (report.getModeratorAnalyst() != null) return;

        User actor = authService.getAuthenticatedUser();
        if (actor != null) {
            report.setModeratorAnalyst(userRoleService.getModeratorByUser(actor));
        }
    }
}
