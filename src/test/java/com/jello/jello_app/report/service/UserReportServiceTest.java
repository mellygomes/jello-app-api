package com.jello.jello_app.report.service;

import com.jello.jello_app.report.dto.UpdateUserReportDTO;
import com.jello.jello_app.report.dto.UserReportRequestDTO;
import com.jello.jello_app.report.dto.UserReportResponseDTO;
import com.jello.jello_app.report.exception.ReportNotFoundException;
import com.jello.jello_app.report.model.UserReport;
import com.jello.jello_app.report.repository.UserReportRepository;
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
class UserReportServiceTest {
    // TODO: NO FRONT CRIAR A TELA DE VISUALIZACAO DE REPORT DE POST (MODERADOR) (TABELA COM TODOS), CRIAR MODAL PARA REPORTAR POSTS
    // TODO: APOS ISSO CRIAR MESMO FLUXO DE TELAS PARA REPORT DE USUARIOS

    @Mock
    private UserReportRepository userReportRepository;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserReportService userReportService;

    private User reportedUser;
    private User reporterUser;
    private Moderator moderator;
    private UserReport report;
    private UserReportRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        reportedUser = createUser(1L, "repotado.com", "reported@mail.com");
        reporterUser = createUser(2L, "reporter.com", "reporter@mail.com");
        moderator = createModerator();
        report = createUserReport();
        createRequest = createRequestDTO();
    }

    @Test
    void shouldCreateUserReport() {
        when(userService.getUserById(1L)).thenReturn(reportedUser);
        when(userService.getUserById(2L)).thenReturn(reporterUser);
        when(userRoleService.getModerator()).thenReturn(moderator);

        userReportService.createUserReport(createRequest, reporterUser.getId());

        verify(userReportRepository).save(any());
    }

    @Test
    void shouldNotCreateReportWhenUserNotFound() {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException(1L));

        assertThrows(UserNotFoundException.class, () -> userReportService.createUserReport(createRequest, reporterUser.getId()));

        verifyNoInteractions(userReportRepository);
    }

    @Test
    void shouldNotCreateReportWhenModeratorNotFound() {
        when(userRoleService.getModerator()).thenThrow(new ModeratorNotFoundException());

        assertThrows(ModeratorNotFoundException.class, () -> userReportService.createUserReport(createRequest, reporterUser.getId()));

        verifyNoInteractions(userReportRepository);
    }

    @Test
    void shouldGetUserReportById() {
        Long id = 1L;

        when(userReportRepository.findById(id)).thenReturn(Optional.of(report));

        UserReport result = userReportService.getUserReportById(id);

        assertNotNull(result);
        verify(userReportRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenUserReportNotFound() {
        Long id = 2L;

        when(userReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> userReportService.getUserReportById(id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 2 não encontrado"));
        verify(userReportRepository).findById(anyLong());
    }

    @Test
    void shouldGetAllUserReports() {
        List<UserReport> reports = List.of(report);
        when(userReportRepository.findAll()).thenReturn(reports);

        List<UserReportResponseDTO> result = userReportService.getAllUsersReports();

        assertEquals(1, result.size());
        verify(userReportRepository).findAll();
    }

    @Test
    void shouldUpdateUserReport() {
        Long id = 1L;

        UpdateUserReportDTO request = new UpdateUserReportDTO();
        request.setContent("Atualizei minha denuncia mesmo");

        when(userReportRepository.findById(id)).thenReturn(Optional.of(report));

        UserReportResponseDTO result = userReportService.updateUserReport(request, report.getId());

        assertTrue(result.getContent().contains("Atualizei minha denuncia mesmo"));
        verify(userReportRepository).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenUpdateUserReportNotFound() {
        Long id = 3L;

        UpdateUserReportDTO request = new UpdateUserReportDTO();
        request.setContent("Atualizando denuncia");

        when(userReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> userReportService.updateUserReport(request, id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 3 não encontrado"));
        verify(userReportRepository).findById(anyLong());
    }

    @Test
    void shouldDeleteUserReport() {
        Long id = 11L;
        when(userReportRepository.findById(id)).thenReturn(Optional.of(report));

        userReportService.deleteUserReport(id);

        verify(userReportRepository).findById(anyLong());
        verify(userReportRepository).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeleteUserReportNotFound() {
        Long id = 11L;

        when(userReportRepository.findById(id)).thenThrow(new ReportNotFoundException(id));

        ReportNotFoundException result = assertThrows(
                ReportNotFoundException.class,
                () -> userReportService.deleteUserReport(id)
        );

        assertTrue(result.getMessage().contains("Report de ID: 11 não encontrado"));
        verify(userReportRepository).findById(anyLong());
        verify(userReportRepository, times(0)).delete(any());
    }


    private User createUser(Long userId, String userName, String email) {
        User user = new User();
        user.setId(userId);
        user.setUsername(userName);
        user.setEmail(email);

        return user;
    }

    private Moderator createModerator() {
        User userMod = new User();
        userMod.setId(5L);

        Moderator moderator = new Moderator();
        moderator.setId(4L);
        moderator.setUser(userMod);

        return moderator;
    }

    private UserReport createUserReport() {
        UserReport report = new UserReport();
        report.setId(1L);
        report.setReportedUser(reportedUser);
        report.setReporterUser(reporterUser);
        report.setModeratorAnalyst(moderator);
        report.setContent("Reportei esse mano aqui porque ele é chato");

        return report;
    }

    private UserReportRequestDTO createRequestDTO() {
        UserReportRequestDTO request = new UserReportRequestDTO();
        request.setContent("Conteúdo da denúncia mesmo");
        request.setUserReportedId(reportedUser.getId());

        return request;
    }
}