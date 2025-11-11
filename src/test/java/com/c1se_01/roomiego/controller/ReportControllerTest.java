package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.common.ResponseData;
import com.c1se_01.roomiego.dto.HandleReportRequest;
import com.c1se_01.roomiego.dto.ReportRequest;
import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

  @Mock
  private ReportService reportService;

  @InjectMocks
  private ReportController reportController;

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  private ReportRequest reportRequest;
  private HandleReportRequest handleReportRequest;
  private ReportResponse reportResponse;
  private ResponseData responseData;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    objectMapper = new ObjectMapper();

    reportRequest = new ReportRequest();
    reportRequest.setRoomId(1L);
    reportRequest.setReason("Test reason");

    handleReportRequest = new HandleReportRequest();
    handleReportRequest.setIsViolation(true);
    handleReportRequest.setAdminNote("Handled");

    reportResponse = ReportResponse.builder()
        .id(1L)
        .roomId(1L)
        .roomTitle("Test Room")
        .roomAddress("123 Test St")
        .reporterName("Test Reporter")
        .reason("Test reason")
        .isHandled(false)
        .isViolation(false)
        .adminNote(null)
        .createdAt(new java.util.Date())
        .build();

    List<ReportResponse> reports = Arrays.asList(reportResponse);
    Page<ReportResponse> page = new PageImpl<>(reports, PageRequest.of(0, 25), 1);
    responseData = new ResponseData(true, "Success", page);
  }

  @Test
  public void testReportRoom_HappyCase() throws Exception {
    // Arrange
    doNothing().when(reportService).reportRoom(any(ReportRequest.class));

    // Act & Assert
    mockMvc.perform(post("/api/reports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(reportRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void testGetReports_HappyCase() throws Exception {
    // Arrange
    when(reportService.getReports(eq(null), any(Pageable.class))).thenReturn(responseData);

    // Act & Assert
    mockMvc.perform(get("/api/reports/admin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].id").value(1L));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void testHandleReport_HappyCase() throws Exception {
    // Arrange
    doNothing().when(reportService).handleReport(eq(1L), any(HandleReportRequest.class));

    // Act & Assert
    mockMvc.perform(post("/api/reports/1/handle")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(handleReportRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void testGetReportDetail_HappyCase() throws Exception {
    // Arrange
    when(reportService.getReportById(1L)).thenReturn(reportResponse);

    // Act & Assert
    mockMvc.perform(get("/api/reports/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L));
  }

  @Test
  public void testDeleteReport_HappyCase() throws Exception {
    // Arrange
    doNothing().when(reportService).deleteReport(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/reports/1"))
        .andExpect(status().isNoContent());
  }
}