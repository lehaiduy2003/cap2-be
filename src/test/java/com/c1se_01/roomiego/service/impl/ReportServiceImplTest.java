package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.common.ResponseData;
import com.c1se_01.roomiego.dto.HandleReportRequest;
import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.ReportRequest;
import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.mapper.ReportMapper;
import com.c1se_01.roomiego.model.Report;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.ReportRepository;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

  @Mock
  private ReportRepository reportRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private ReportMapper reportMapper;

  @Mock
  private NotificationService notificationService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private UserRepository userRepository;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private ReportServiceImpl reportService;

  private User reporter;
  private User admin;
  private User owner;
  private Room room;
  private Report report;
  private ReportRequest reportRequest;
  private HandleReportRequest handleReportRequest;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    reporter = new User();
    reporter.setId(1L);
    reporter.setFullName("Reporter Name");

    admin = new User();
    admin.setId(2L);
    admin.setRole(Role.ADMIN);

    owner = new User();
    owner.setId(3L);

    room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    report = new Report();
    report.setId(1L);
    report.setRoom(room);
    report.setReporter(reporter);
    report.setReason("Test reason");
    report.setIsHandled(false);

    reportRequest = new ReportRequest();
    reportRequest.setRoomId(1L);
    reportRequest.setReason("Test reason");

    handleReportRequest = new HandleReportRequest();
    handleReportRequest.setIsViolation(true);
    handleReportRequest.setAdminNote("Violation note");

    pageable = PageRequest.of(0, 10);
  }

  // Tests for reportRoom
  @Test
  void reportRoom_SuccessfulReport() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(reporter);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(userRepository.findByRole(Role.ADMIN)).thenReturn(Optional.of(admin));
      when(reportRepository.save(any(Report.class))).thenReturn(report);

      reportService.reportRoom(reportRequest);

      verify(reportRepository, times(1)).save(any(Report.class));
      verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + admin.getId()),
          any(NotificationDto.class));
    }
  }

  @Test
  void reportRoom_RoomNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(roomRepository.findById(1L)).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.reportRoom(reportRequest));
      assertEquals("Room not found", exception.getMessage());
    }
  }

  // Tests for getReports
  @Test
  void getReports_IsHandledNull() {
    List<Report> reports = List.of(report);
    Page<Report> page = new PageImpl<>(reports, pageable, 1);
    List<ReportResponse> reportResponses = Collections.singletonList(ReportResponse.builder().build());

    when(reportRepository.findAll(pageable)).thenReturn(page);
    when(reportMapper.toDto(reports)).thenReturn(reportResponses);

    ResponseData result = reportService.getReports(null, pageable);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMessage());
    verify(reportRepository, times(1)).findAll(pageable);
    verify(reportMapper, times(1)).toDto(reports);
  }

  @Test
  void getReports_IsHandledTrue() {
    List<Report> reports = List.of(report);
    Page<Report> page = new PageImpl<>(reports, pageable, 1);
    List<ReportResponse> reportResponses = Collections.singletonList(ReportResponse.builder().build());

    when(reportRepository.findAllByIsHandled(true, pageable)).thenReturn(Optional.of(page));
    when(reportMapper.toDto(reports)).thenReturn(reportResponses);

    ResponseData result = reportService.getReports(true, pageable);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMessage());
    verify(reportRepository, times(1)).findAllByIsHandled(true, pageable);
    verify(reportMapper, times(1)).toDto(reports);
  }

  @Test
  void getReports_IsHandledFalse() {
    List<Report> reports = List.of(report);
    Page<Report> page = new PageImpl<>(reports, pageable, 1);
    List<ReportResponse> reportResponses = Collections.singletonList(ReportResponse.builder().build());

    when(reportRepository.findAllByIsHandled(false, pageable)).thenReturn(Optional.of(page));
    when(reportMapper.toDto(reports)).thenReturn(reportResponses);

    ResponseData result = reportService.getReports(false, pageable);

    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMessage());
    verify(reportRepository, times(1)).findAllByIsHandled(false, pageable);
    verify(reportMapper, times(1)).toDto(reports);
  }

  @Test
  void getReports_IsHandledTrue_NoReportsFound() {
    when(reportRepository.findAllByIsHandled(true, pageable)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.getReports(true, pageable));
    assertEquals("Report not found", exception.getMessage());
  }

  @Test
  void getReports_IsHandledFalse_NoReportsFound() {
    when(reportRepository.findAllByIsHandled(false, pageable)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.getReports(false, pageable));
    assertEquals("Report not found", exception.getMessage());
  }

  // Tests for handleReport
  @Test
  void handleReport_IsViolationTrue() {
    when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

    reportService.handleReport(1L, handleReportRequest);

    verify(reportRepository, times(1)).save(report);
    assertTrue(report.getIsHandled());
    assertTrue(report.getIsViolation());
    assertEquals("Violation note", report.getAdminNote());
    verify(reportRepository, times(1)).deleteByRoom(room);
    verify(roomRepository, times(1)).delete(room);
    verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
    verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + owner.getId()),
        any(NotificationDto.class));
  }

  @Test
  void handleReport_IsViolationFalse() {
    handleReportRequest.setIsViolation(false);

    when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

    reportService.handleReport(1L, handleReportRequest);

    verify(reportRepository, times(1)).save(report);
    assertTrue(report.getIsHandled());
    assertFalse(report.getIsViolation());
    assertEquals("Violation note", report.getAdminNote());
    verify(reportRepository, never()).deleteByRoom(any(Room.class));
    verify(roomRepository, never()).delete(any(Room.class));
    verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
    verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + reporter.getId()),
        any(NotificationDto.class));
  }

  @Test
  void handleReport_ReportNotFound() {
    when(reportRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> reportService.handleReport(1L, handleReportRequest));
    assertEquals("Report not found", exception.getMessage());
  }

  // Tests for getReportById
  @Test
  void getReportById_Success() {
    ReportResponse reportResponse = ReportResponse.builder().build();

    when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
    when(reportMapper.toDto(report)).thenReturn(reportResponse);

    ReportResponse result = reportService.getReportById(1L);

    assertEquals(reportResponse, result);
    verify(reportRepository, times(1)).findById(1L);
    verify(reportMapper, times(1)).toDto(report);
  }

  @Test
  void getReportById_ReportNotFound() {
    when(reportRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.getReportById(1L));
    assertEquals("Report not found with id: 1", exception.getMessage());
  }

  // Tests for deleteReport
  @Test
  void deleteReport_Success() {
    when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

    reportService.deleteReport(1L);

    verify(reportRepository, times(1)).delete(report);
  }

  @Test
  void deleteReport_ReportNotFound() {
    when(reportRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.deleteReport(1L));
    assertEquals("Report not found with id: 1", exception.getMessage());
  }
}