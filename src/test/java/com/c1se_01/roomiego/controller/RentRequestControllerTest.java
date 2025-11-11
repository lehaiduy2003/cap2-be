package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.RentRequestCreateRequest;
import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.dto.RentRequestUpdateRequest;
import com.c1se_01.roomiego.enums.RentRequestStatus;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.RentRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RentRequestControllerTest {

  @Mock
  private RentRequestService rentRequestService;

  @InjectMocks
  private RentRequestController rentRequestController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private User mockUser;
  private RentRequestResponse mockRentRequestResponse;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(rentRequestController).build();
    objectMapper = new ObjectMapper();

    // Setup mock user
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setFullName("Test User");

    // Setup mock authentication
    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Setup mock RentRequestResponse
    mockRentRequestResponse = new RentRequestResponse();
    mockRentRequestResponse.setId(1L);
    mockRentRequestResponse.setTenantId(1L);
    mockRentRequestResponse.setRoomId(1L);
    mockRentRequestResponse.setStatus(RentRequestStatus.PENDING);
    mockRentRequestResponse.setCreatedAt(new Date());
    mockRentRequestResponse.setTenantFinalize(false);
    mockRentRequestResponse.setOwnerFinalize(false);
    mockRentRequestResponse.setMessage("Test message");
    mockRentRequestResponse.setAdminNote("Test admin note");
  }

  // Create Rent Request Tests
  @Test
  public void testCreateRentRequest_HappyCase() throws Exception {
    // Arrange
    RentRequestCreateRequest request = new RentRequestCreateRequest();
    request.setRoomId(1L);

    when(rentRequestService.createRentRequest(any(RentRequestCreateRequest.class))).thenReturn(mockRentRequestResponse);

    // Act & Assert
    mockMvc.perform(post("/api/rent-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.tenantId").value(1L))
        .andExpect(jsonPath("$.roomId").value(1L))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  public void testCreateRentRequest_NullRequest() throws Exception {
    // Arrange
    when(rentRequestService.createRentRequest(any(RentRequestCreateRequest.class))).thenReturn(mockRentRequestResponse);

    // Act & Assert
    mockMvc.perform(post("/api/rent-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isCreated());
  }

  @Test
  public void testCreateRentRequest_NullRoomId() throws Exception {
    // Arrange
    RentRequestCreateRequest request = new RentRequestCreateRequest();
    request.setRoomId(null);

    when(rentRequestService.createRentRequest(any(RentRequestCreateRequest.class))).thenReturn(mockRentRequestResponse);

    // Act & Assert
    mockMvc.perform(post("/api/rent-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  // Get Requests By Owner Tests
  @Test
  public void testGetRequestsByOwner_HappyCase() throws Exception {
    // Arrange
    List<RentRequestResponse> responses = Arrays.asList(mockRentRequestResponse);
    when(rentRequestService.getRequestsByOwner()).thenReturn(responses);

    // Act & Assert
    mockMvc.perform(get("/api/rent-requests/owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  public void testGetRequestsByOwner_EmptyList() throws Exception {
    // Arrange
    List<RentRequestResponse> responses = Collections.emptyList();
    when(rentRequestService.getRequestsByOwner()).thenReturn(responses);

    // Act & Assert
    mockMvc.perform(get("/api/rent-requests/owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  // Update Rent Request Status Tests
  @Test
  public void testUpdateRentRequestStatus_HappyCase_Approved() throws Exception {
    // Arrange
    RentRequestUpdateRequest updateRequest = new RentRequestUpdateRequest();
    updateRequest.setStatus(RentRequestStatus.APPROVED);

    RentRequestResponse updatedResponse = new RentRequestResponse();
    updatedResponse.setId(1L);
    updatedResponse.setStatus(RentRequestStatus.APPROVED);

    when(rentRequestService.updateRentRequestStatus(anyLong(), any(RentRequestUpdateRequest.class)))
        .thenReturn(updatedResponse);

    // Act & Assert
    mockMvc.perform(put("/api/rent-requests/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }

  @Test
  public void testUpdateRentRequestStatus_HappyCase_Rejected() throws Exception {
    // Arrange
    RentRequestUpdateRequest updateRequest = new RentRequestUpdateRequest();
    updateRequest.setStatus(RentRequestStatus.REJECTED);

    RentRequestResponse updatedResponse = new RentRequestResponse();
    updatedResponse.setId(1L);
    updatedResponse.setStatus(RentRequestStatus.REJECTED);

    when(rentRequestService.updateRentRequestStatus(anyLong(), any(RentRequestUpdateRequest.class)))
        .thenReturn(updatedResponse);

    // Act & Assert
    mockMvc.perform(put("/api/rent-requests/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.status").value("REJECTED"));
  }

  @Test
  public void testUpdateRentRequestStatus_NullStatus() throws Exception {
    // Arrange
    RentRequestUpdateRequest updateRequest = new RentRequestUpdateRequest();
    updateRequest.setStatus(null);

    when(rentRequestService.updateRentRequestStatus(anyLong(), any(RentRequestUpdateRequest.class)))
        .thenReturn(mockRentRequestResponse);

    // Act & Assert
    mockMvc.perform(put("/api/rent-requests/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk());
  }

  @Test
  public void testUpdateRentRequestStatus_InvalidRequestId() throws Exception {
    // Arrange
    RentRequestUpdateRequest updateRequest = new RentRequestUpdateRequest();
    updateRequest.setStatus(RentRequestStatus.APPROVED);

    // Act & Assert - Invalid path variable should result in 400 Bad Request
    mockMvc.perform(put("/api/rent-requests/abc")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }

  // Cancel Rental Tests
  @Test
  public void testCancelRental_HappyCase() throws Exception {
    // Arrange
    RentRequestResponse cancelledResponse = new RentRequestResponse();
    cancelledResponse.setId(1L);
    cancelledResponse.setStatus(RentRequestStatus.REJECTED);

    when(rentRequestService.cancelRental(anyLong())).thenReturn(cancelledResponse);

    // Act & Assert
    mockMvc.perform(post("/api/rent-requests/1/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.status").value("REJECTED"));
  }

  @Test
  public void testCancelRental_InvalidRequestId() throws Exception {
    // Arrange & Act & Assert - Invalid path variable should result in 400 Bad
    // Request
    mockMvc.perform(post("/api/rent-requests/abc/cancel"))
        .andExpect(status().isBadRequest());
  }
}