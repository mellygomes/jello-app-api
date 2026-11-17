package com.jello.jello_app.report.service;

import com.jello.jello_app.post.exception.PostNotFoundException;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.report.dto.PostReportRequestDTO;
import com.jello.jello_app.report.dto.PostReportResponseDTO;
import com.jello.jello_app.report.dto.UpdatePostReportDTO;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.model.PostReport;
import com.jello.jello_app.report.repository.PostReportRepository;
import com.jello.jello_app.role.exception.ModeratorNotFoundException;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostReportServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PostReportRepository postReportRepository;

    @InjectMocks
    private PostReportService postReportService;

    private User user;
    private Post post;
    private Moderator moderator;
    private PostReport report;
    private PostReportRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        user = createUser();
        post = createPost();
        moderator = createModerator();
        report = createPostReport();
        createRequest = createRequestDTO();
    }

    @Test
    void shouldCreatePostReport() {
        when(userService.getUserById(1L)).thenReturn(user);
        when(postService.getPostById(2L)).thenReturn(post);
        when(userRoleService.getModerator()).thenReturn(moderator);

        postReportService.createPostReport(createRequest, user.getId());

        verify(postReportRepository).save(any());
    }

    @Test
    void shouldNotCreateReportWhenUserNotFound() {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException(1L));

        assertThrows(UserNotFoundException.class , () -> postReportService.createPostReport(createRequest, user.getId()));

        verifyNoInteractions(postReportRepository);
    }

    @Test
    void shouldNotCreateReportWhenPostNotFound() {
        when(postService.getPostById(2L)).thenThrow(new PostNotFoundException(1L));

        assertThrows(PostNotFoundException.class , () -> postReportService.createPostReport(createRequest, user.getId()));

        verifyNoInteractions(postReportRepository);
    }

    @Test
    void shouldNotCreateReportWhenModeratorNotFound() {
        when(userRoleService.getModerator()).thenThrow(new ModeratorNotFoundException());

        assertThrows(ModeratorNotFoundException.class , () -> postReportService.createPostReport(createRequest, user.getId()));

        verifyNoInteractions(postReportRepository);
    }

    @Test
    void shouldGetPostReportById() {
        Long id = 1L;

        when(postReportRepository.findById(id)).thenReturn(Optional.of(report));

        PostReport result = postReportService.getPostReportById(id);

        assertNotNull(result);
        verify(postReportRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenPostReportNotFound() {
        Long id = 2L;

        when(postReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> postReportService.getPostReportById(id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 2 não encontrado"));
        verify(postReportRepository).findById(anyLong());
    }

    @Test
    void shouldGetAllPostReports() {
        List<PostReport> reports = List.of(report);
        when(postReportRepository.findAll()).thenReturn(reports);

        List<PostReportResponseDTO> result = postReportService.getAllPostReports();

        assertEquals(1, result.size());
        verify(postReportRepository).findAll();
    }

    @Test
    void shouldUpdatePostReport() {
        Long id = 1L;

        UpdatePostReportDTO request = new UpdatePostReportDTO();
        request.setContent("Report feito pois o post apresenta conteudos deveras ofensivos!");

        when(postReportRepository.findById(id)).thenReturn(Optional.of(report));

        PostReportResponseDTO result = postReportService.updatePostReport(request, report.getId());

        assertTrue(result.getContent().contains("Report feito pois o post apresenta conteudos deveras ofensivos!"));
        verify(postReportRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenUpdatePostReportNotFound() {
        Long id = 3L;

        UpdatePostReportDTO request = new UpdatePostReportDTO();
        request.setContent("Quero alterar meu post mas a requisição vai falhar :(");

        when(postReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> postReportService.updatePostReport(request, id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 3 não encontrado"));
        verify(postReportRepository).findById(anyLong());
    }

    @Test
    void shouldDeletePostReport() {
        Long id = 11L;
        when(postReportRepository.findById(id)).thenReturn(Optional.of(report));

        postReportService.deletePostReport(id);

        verify(postReportRepository).findById(anyLong());
        verify(postReportRepository).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletePostReportNotFound() {
        Long id = 11L;

        when(postReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> postReportService.deletePostReport(id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 11 não encontrado"));
        verify(postReportRepository).findById(anyLong());
        verify(postReportRepository, times(0)).delete(any());
    }


    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("nickname");
        user.setEmail("email@teste.com");

        return user;
    }

    private Post createPost() {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("Titulo post teste");
        post.setContent("Conteudo do post");
        post.setUser(user);

        return post;
    }

    private Moderator createModerator() {
        User userMod = new User();
        userMod.setId(5L);

        Moderator moderator = new Moderator();
        moderator.setId(4L);
        moderator.setUser(userMod);

        return moderator;
    }

    private PostReport createPostReport() {
        PostReport report = new PostReport();
        report.setId(1L);
        report.setReporterUser(user);
        report.setReportedPost(post);
        report.setModeratorAnalyst(moderator);
        report.setContent("Reportei esse post porque nao gostei dele mesmo");

        return report;
    }

    private PostReportRequestDTO createRequestDTO() {
        PostReportRequestDTO request = new PostReportRequestDTO();
        request.setPostId(post.getId());
        request.setContent("Conteúdo da denúncia mesmo");

        return request;
    }
}