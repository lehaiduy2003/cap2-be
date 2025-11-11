package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.dto.ViewRespondDTO;
import com.c1se_01.roomiego.enums.ViewRequestStatus;
import com.c1se_01.roomiego.service.ViewRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ViewRequestControllerTest {

  @Mock
  private ViewRequestService viewRequestService;

  @InjectMocks
  private ViewRequestController viewRequestController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  private ViewRequestCreateDTO createDTO;
  private ViewRequestDTO viewRequestDTO;
  private ViewRespondDTO respondDTO;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(viewRequestController).build();
    objectMapper = new ObjectMapper();

    createDTO = new ViewRequestCreateDTO();
    createDTO.setRoomId(1L);
    createDTO.setMessage("Test message");

    viewRequestDTO = new ViewRequestDTO();
    viewRequestDTO.setId(1L);
    viewRequestDTO.setRoomId(1L);
    viewRequestDTO.setRenterId(2L);
    viewRequestDTO.setOwnerId(3L);
    viewRequestDTO.setMessage("Test message");
    viewRequestDTO.setStatus(ViewRequestStatus.PENDING);
    viewRequestDTO.setAdminNote(null);
    viewRequestDTO.setCreatedAt(new Date());

    respondDTO = new ViewRespondDTO();
    respondDTO.setRequestId(1L);
    respondDTO.setAccept(true);
    respondDTO.setAdminNote("Approved");
  }

  // createRequest Tests
  @Test
  @WithMockUser(roles = "RENTER")
  public void testCreateRequest_HappyCase() throws Exception {
    when(viewRequestService.createRequest(any(ViewRequestCreateDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.roomId").value(1L))
        .andExpect(jsonPath("$.message").value("Test message"));
  }

  @Test
  @WithMockUser(roles = "RENTER")
  public void testCreateRequest_NullMessage() throws Exception {
    createDTO.setMessage(null);
    when(viewRequestService.createRequest(any(ViewRequestCreateDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated());
  }

  // getRequestsByOwner Tests
  @Test
  @WithMockUser(roles = "OWNER")
  public void testGetRequestsByOwner_HappyCase() throws Exception {
    List<ViewRequestDTO> requests = Arrays.asList(viewRequestDTO);
    when(viewRequestService.getRequestsByOwner()).thenReturn(requests);

    mockMvc.perform(get("/api/view-requests/owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].roomId").value(1L));
  }

  @Test
  @WithMockUser(roles = "OWNER")
  public void testGetRequestsByOwner_EmptyList() throws Exception {
    List<ViewRequestDTO> requests = Collections.emptyList();
    when(viewRequestService.getRequestsByOwner()).thenReturn(requests);

    mockMvc.perform(get("/api/view-requests/owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  // respondToRequest Tests
  @Test
  @WithMockUser(roles = "OWNER")
  public void testRespondToRequest_HappyCase() throws Exception {
    when(viewRequestService.respondToRequest(any(ViewRespondDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests/respond")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(respondDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @WithMockUser(roles = "OWNER")
  public void testRespondToRequest_Reject() throws Exception {
    respondDTO.setAccept(false);
    respondDTO.setAdminNote("Rejected");
    when(viewRequestService.respondToRequest(any(ViewRespondDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests/respond")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(respondDTO)))
        .andExpect(status().isOk());
  }

  // cancelRental Tests
  @Test
  @WithMockUser(roles = "OWNER")
  public void testCancelRental_HappyCase() throws Exception {
    when(viewRequestService.cancelRental(any(ViewRespondDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests/cancel-rental")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(respondDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L));
  }

  @Test
  @WithMockUser(roles = "OWNER")
  public void testCancelRental_NullAdminNote() throws Exception {
    respondDTO.setAdminNote(null);
    when(viewRequestService.cancelRental(any(ViewRespondDTO.class))).thenReturn(viewRequestDTO);

    mockMvc.perform(post("/api/view-requests/cancel-rental")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(respondDTO)))
        .andExpect(status().isOk());
  }
}