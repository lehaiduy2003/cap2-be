package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.AiRecommendationDTO;
import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.service.RoommateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoomateControllerTest {

  @Mock
  private RoommateService roommateService;

  @InjectMocks
  private RoomateController roomateController;

  private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(roomateController)
        .setValidator(validator)
        .build();
  }

  @Test
  public void testCreateRoommate_HappyCase() throws Exception {
    // Arrange
    RoommateDTO dto = new RoommateDTO();
    dto.setUserId(1L);
    dto.setHometown("Hanoi");
    dto.setCity("Hanoi");
    dto.setDistrict("Ba Dinh");

    RoommateResponseDTO responseDTO = new RoommateResponseDTO();
    responseDTO.setUserId(1L);
    responseDTO.setHometown("Hanoi");

    when(roommateService.createRoommate(any(RoommateDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/roommates")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo thông tin roommate thành công"))
        .andExpect(jsonPath("$.data.userId").value(1));
  }

  @Test
  public void testCreateRoommate_InvalidUserId() throws Exception {
    // Arrange
    RoommateDTO dto = new RoommateDTO();
    dto.setUserId(null); // Invalid

    // Act & Assert
    mockMvc.perform(post("/api/roommates")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetAllRoommates_HappyCase() throws Exception {
    // Arrange
    RoommateResponseDTO roommate1 = new RoommateResponseDTO();
    roommate1.setUserId(1L);
    RoommateResponseDTO roommate2 = new RoommateResponseDTO();
    roommate2.setUserId(2L);
    List<RoommateResponseDTO> roommates = Arrays.asList(roommate1, roommate2);

    when(roommateService.getAllRoommates()).thenReturn(roommates);

    // Act & Assert
    mockMvc.perform(get("/api/roommates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Danh sách roommate"))
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  public void testExportRoommatesToFile_HappyCase() throws Exception {
    // Arrange
    RoommateResponseDTO roommate = new RoommateResponseDTO();
    roommate.setUserId(1L);
    List<RoommateResponseDTO> roommates = Arrays.asList(roommate);

    when(roommateService.getAllRoommates()).thenReturn(roommates);

    // Act & Assert
    mockMvc.perform(get("/api/roommates/export-to-file"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Xuất dữ liệu thành công"))
        .andExpect(jsonPath("$.data.filePath").value("D:/RoomieGO/roommate_finder/data.json"));
  }

  @Test
  public void testGetRecommendations_HappyCase() throws Exception {
    // Arrange
    Long userId = 1L;
    AiRecommendationDTO rec1 = new AiRecommendationDTO();
    rec1.setUserId(2L);
    List<AiRecommendationDTO> recommendations = Arrays.asList(rec1);

    when(roommateService.getRecommendations(userId)).thenReturn(recommendations);

    // Act & Assert
    mockMvc.perform(get("/api/roommates/recommend/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Gợi ý roommate thành công"))
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  public void testGetRecommendations_InvalidUserId() throws Exception {
    // Arrange
    String invalidUserId = "abc";

    // Act & Assert
    mockMvc.perform(get("/api/roommates/recommend/{userId}", invalidUserId))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetRecommendations_ServiceThrowsException() throws Exception {
    // Arrange
    Long userId = 1L;

    when(roommateService.getRecommendations(userId)).thenThrow(new RuntimeException("Service error"));

    // Act & Assert
    mockMvc.perform(get("/api/roommates/recommend/{userId}", userId))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("Lỗi khi lấy gợi ý roommate"));
  }
}