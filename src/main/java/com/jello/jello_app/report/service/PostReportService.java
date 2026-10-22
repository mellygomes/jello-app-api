package com.jello.jello_app.report.service;

import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.report.dto.PostReportRequestDTO;
import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UpdatePostReportDTO;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.mapper.ReportMapper;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.repository.PostReportRepository;
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

    @Transactional
    public PostReport createPostReport(PostReportRequestDTO request, Long userId) {
        User user = userService.getUserById(userId);
        Post post = postService.getPostById(request.getPostId());
        Moderator moderator = userRoleService.getModerator();

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
}
