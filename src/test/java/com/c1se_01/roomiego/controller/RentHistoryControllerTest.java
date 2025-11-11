package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.RentHistoryCreateRequest;
import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.service.RentHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RentHistoryControllerTest {

  @Mock
  private RentHistoryService rentHistoryService;

  @InjectMocks
  private RentHistoryController rentHistoryController;

  private MockMvc mockMvc;

  private RentHistoryCreateRequest createRequest;
  private RentHistoryResponse response;

  @BeforeEach
  public void setUp() {
    RentHistoryController controller = new RentHistoryController(rentHistoryService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    createRequest = new RentHistoryCreateRequest();
    createRequest.setUserId(1L);
    createRequest.setRentRequestId(1L);
    createRequest.setRentDate(LocalDateTime.now());
    createRequest.setReturnDate(LocalDateTime.now().plusDays(30));

    response = new RentHistoryResponse();
    response.setId(1L);
    response.setUserId(1L);
    response.setRentRequestId(1L);
    response.setRoomId(1L);
    response.setRoomTitle("Test Room");
    response.setAddress("123 Test St");
    response.setDescription("Test Description");
    response.setLongitude(10.0);
    response.setLatitude(20.0);
    response.setRentDate(LocalDateTime.now());
    response.setReturnDate(LocalDateTime.now().plusDays(30));
    response.setReviewed(false);
  }

  @Test
  public void testCreateRentHistory_Success() throws Exception {
    // Arrange
    when(rentHistoryService.createRentHistory(any(RentHistoryCreateRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/rent-histories")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"userId\":1,\"rentRequestId\":1,\"rentDate\":\"2023-01-01T10:00:00\",\"returnDate\":\"2023-01-31T10:00:00\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.userId").value(1L))
        .andExpect(jsonPath("$.rentRequestId").value(1L))
        .andExpect(jsonPath("$.roomTitle").value("Test Room"));
  }

  @Test
  public void testGetRentHistoriesByUser_Success() throws Exception {
    // Arrange
    List<RentHistoryResponse> responses = Arrays.asList(response);
    when(rentHistoryService.getRentHistoriesByUser(1L)).thenReturn(responses);

    // Act & Assert
    mockMvc.perform(get("/api/rent-histories/user/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].userId").value(1L))
        .andExpect(jsonPath("$[0].roomTitle").value("Test Room"));
  }

  @Test
  public void testAddReviewToRentHistory_Success() throws Exception {
    // Arrange
    // No mocking needed for void method

    // Act & Assert
    mockMvc.perform(patch("/api/rent-histories/reviews/1"))
        .andExpect(status().isNoContent());
  }
}