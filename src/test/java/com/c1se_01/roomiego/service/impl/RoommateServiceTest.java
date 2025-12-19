package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.AiRecommendationDTO;
import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.enums.Gender;
import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RoommateRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoommateServiceTest {

  @Mock
  private RoommateRepository roommateRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private RoommateServiceImpl roommateService;

  private User currentUser;
  private RoommateDTO roommateDTO;
  private Roommate roommate;
  private RoommateResponseDTO roommateResponseDTO;

  @BeforeEach
  void setUp() {
    // Setup current user
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");
    currentUser.setGender(Gender.MALE);

    // Setup roommate DTO
    roommateDTO = new RoommateDTO();
    roommateDTO.setHometown("Hanoi");
    roommateDTO.setCity("Hanoi");
    roommateDTO.setDistrict("Hoan Kiem");
    roommateDTO.setRateImage(8);
    roommateDTO.setYob(1995);
    roommateDTO.setJob("Engineer");
    roommateDTO.setHobbies("Reading");
    roommateDTO.setMore("Friendly person");
    roommateDTO.setPhone("123456789");
    roommateDTO.setUserId(1L);

    // Setup roommate entity
    roommate = new Roommate();
    roommate.setId(1L);
    roommate.setGender("MALE");
    roommate.setHometown("Hanoi");
    roommate.setCity("Hanoi");
    roommate.setDistrict("Hoan Kiem");
    roommate.setRateImage(8);
    roommate.setYob(1995);
    roommate.setJob("Engineer");
    roommate.setHobbies("Reading");
    roommate.setMore("Friendly person");
    roommate.setPhone("123456789");
    roommate.setUser(currentUser);

    // Setup response DTO
    roommateResponseDTO = new RoommateResponseDTO();
    roommateResponseDTO.setGender("MALE");
    roommateResponseDTO.setHometown("Hanoi");
    roommateResponseDTO.setCity("Hanoi");
    roommateResponseDTO.setDistrict("Hoan Kiem");
    roommateResponseDTO.setRateImage(8);
    roommateResponseDTO.setYob(1995);
    roommateResponseDTO.setJob("Engineer");
    roommateResponseDTO.setHobbies("Reading");
    roommateResponseDTO.setMore("Friendly person");
    roommateResponseDTO.setPhone("123456789");
    roommateResponseDTO.setUserId(1L);

    // Mock SecurityContext
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    lenient().when(authentication.getPrincipal()).thenReturn(currentUser);
    SecurityContextHolder.setContext(securityContext);

    // Set AI service URL
    ReflectionTestUtils.setField(roommateService, "aiServiceUrl", "http://localhost:8000");
    ReflectionTestUtils.setField(roommateService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(roommateService, "objectMapper", objectMapper);
  }

  // Tests for createRoommate method

  @Test
  void createRoommate_NoExistingRecord_CreatesNewRoommate() {
    // No existing roommate for this user
    when(roommateRepository.findAllByUser(currentUser)).thenReturn(new ArrayList<>());
    when(roommateRepository.save(any(Roommate.class))).thenReturn(roommate);

    RoommateResponseDTO result = roommateService.createRoommate(roommateDTO);

    assertNotNull(result);
    assertEquals("MALE", result.getGender());
    assertEquals("Hanoi", result.getHometown());
    assertEquals("Hanoi", result.getCity());
    assertEquals("Hoan Kiem", result.getDistrict());
    assertEquals(8, result.getRateImage());
    assertEquals(1995, result.getYob());
    assertEquals("Engineer", result.getJob());
    assertEquals("Reading", result.getHobbies());
    assertEquals("Friendly person", result.getMore());
    assertEquals("123456789", result.getPhone());
    assertEquals(1L, result.getUserId());

    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    verify(roommateRepository, times(1)).save(any(Roommate.class));
    verify(roommateRepository, never()).delete(any(Roommate.class));
  }

  @Test
  void createRoommate_OneExistingRecord_UpdatesExistingRoommate() {
    // One existing roommate for this user
    Roommate existingRoommate = new Roommate();
    existingRoommate.setId(1L);
    existingRoommate.setUser(currentUser);
    existingRoommate.setHometown("Old Hometown");

    when(roommateRepository.findAllByUser(currentUser)).thenReturn(Arrays.asList(existingRoommate));
    when(roommateRepository.save(any(Roommate.class))).thenReturn(roommate);

    RoommateResponseDTO result = roommateService.createRoommate(roommateDTO);

    assertNotNull(result);
    assertEquals("Hanoi", result.getHometown()); // Updated value

    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    verify(roommateRepository, times(1)).save(any(Roommate.class));
    verify(roommateRepository, never()).delete(any(Roommate.class));
  }

  @Test
  void createRoommate_MultipleDuplicateRecords_KeepsNewestAndDeletesOthers() {
    // Multiple duplicate roommates for this user
    Roommate oldRoommate1 = new Roommate();
    oldRoommate1.setId(1L);
    oldRoommate1.setUser(currentUser);
    oldRoommate1.setHometown("Old Hometown 1");

    Roommate oldRoommate2 = new Roommate();
    oldRoommate2.setId(2L);
    oldRoommate2.setUser(currentUser);
    oldRoommate2.setHometown("Old Hometown 2");

    Roommate newestRoommate = new Roommate();
    newestRoommate.setId(3L);
    newestRoommate.setUser(currentUser);
    newestRoommate.setHometown("Newest Hometown");

    // Return in random order - service should sort by ID descending
    when(roommateRepository.findAllByUser(currentUser))
        .thenReturn(Arrays.asList(oldRoommate1, newestRoommate, oldRoommate2));
    when(roommateRepository.save(any(Roommate.class))).thenReturn(roommate);

    RoommateResponseDTO result = roommateService.createRoommate(roommateDTO);

    assertNotNull(result);

    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    verify(roommateRepository, times(1)).save(any(Roommate.class));
    // Should delete 2 old records (ID 1 and 2), keep newest (ID 3)
    verify(roommateRepository, times(2)).delete(any(Roommate.class));
  }

  @Test
  void createRoommate_TwoDuplicateRecords_KeepsNewestAndDeletesOlder() {
    // Two duplicate roommates for this user
    Roommate olderRoommate = new Roommate();
    olderRoommate.setId(1L);
    olderRoommate.setUser(currentUser);
    olderRoommate.setHometown("Older Hometown");

    Roommate newerRoommate = new Roommate();
    newerRoommate.setId(2L);
    newerRoommate.setUser(currentUser);
    newerRoommate.setHometown("Newer Hometown");

    when(roommateRepository.findAllByUser(currentUser))
        .thenReturn(Arrays.asList(olderRoommate, newerRoommate));
    when(roommateRepository.save(any(Roommate.class))).thenReturn(roommate);

    RoommateResponseDTO result = roommateService.createRoommate(roommateDTO);

    assertNotNull(result);

    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    verify(roommateRepository, times(1)).save(any(Roommate.class));
    // Should delete 1 old record (ID 1), keep newer (ID 2)
    verify(roommateRepository, times(1)).delete(olderRoommate);
  }

  @Test
  void createRoommate_SaveFails_ThrowsRuntimeException() {
    when(roommateRepository.findAllByUser(currentUser)).thenReturn(new ArrayList<>());
    when(roommateRepository.save(any(Roommate.class))).thenThrow(new RuntimeException("Database error"));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      roommateService.createRoommate(roommateDTO);
    });

    assertEquals("Database error", exception.getMessage());
    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    verify(roommateRepository, times(1)).save(any(Roommate.class));
  }

  @Test
  void createRoommate_UpdateExistingRecord_PreservesId() {
    // Existing roommate with specific ID
    Roommate existingRoommate = new Roommate();
    existingRoommate.setId(99L);
    existingRoommate.setUser(currentUser);
    existingRoommate.setHometown("Old Hometown");

    Roommate savedRoommate = new Roommate();
    savedRoommate.setId(99L); // Same ID preserved
    savedRoommate.setUser(currentUser);
    savedRoommate.setGender("MALE");
    savedRoommate.setHometown("Hanoi");
    savedRoommate.setCity("Hanoi");
    savedRoommate.setDistrict("Hoan Kiem");
    savedRoommate.setRateImage(8);
    savedRoommate.setYob(1995);
    savedRoommate.setJob("Engineer");
    savedRoommate.setHobbies("Reading");
    savedRoommate.setMore("Friendly person");
    savedRoommate.setPhone("123456789");

    when(roommateRepository.findAllByUser(currentUser)).thenReturn(Arrays.asList(existingRoommate));
    when(roommateRepository.save(any(Roommate.class))).thenReturn(savedRoommate);

    RoommateResponseDTO result = roommateService.createRoommate(roommateDTO);

    assertNotNull(result);
    // Verify the response contains updated data
    assertEquals("Hanoi", result.getHometown());
    assertEquals(1L, result.getUserId()); // UserId is the User's ID, not Roommate's ID

    verify(roommateRepository, times(1)).findAllByUser(currentUser);
    // Verify save was called with the existing roommate (ID 99), not a new one
    verify(roommateRepository, times(1)).save(argThat(roommate -> 
        roommate.getId() != null && roommate.getId().equals(99L)
    ));
  }

  // Tests for getAllRoommates method
  @Test
  void getAllRoommates_UserExists_ReturnsFilteredRoommates() {
    // Setup another user and roommate
    User otherUser = new User();
    otherUser.setId(2L);
    otherUser.setEmail("other@example.com");
    otherUser.setGender(Gender.MALE);

    Roommate otherRoommate = new Roommate();
    otherRoommate.setId(2L);
    otherRoommate.setGender("MALE");
    otherRoommate.setHometown("HCM");
    otherRoommate.setUser(otherUser);

    when(authentication.getName()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    lenient().when(roommateRepository.findAllByGender("MALE")).thenReturn(Arrays.asList(otherRoommate));

    List<RoommateResponseDTO> result = roommateService.getAllRoommates();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("MALE", result.get(0).getGender());
    assertEquals("HCM", result.get(0).getHometown());
    assertEquals(2L, result.get(0).getUserId());

    verify(userRepository, times(1)).findByEmail("test@example.com");
    verify(roommateRepository, times(1)).findAllByGender("MALE");
  }

  @Test
  void getAllRoommates_UserNotFound_ReturnsEmptyList() {
    when(authentication.getName()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    List<RoommateResponseDTO> result = roommateService.getAllRoommates();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(userRepository, times(1)).findByEmail("test@example.com");
    verify(roommateRepository, never()).findAllByGender(anyString());
  }

  @Test
  void getAllRoommates_NoRoommatesFound_ReturnsEmptyList() {
    when(authentication.getName()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    lenient().when(roommateRepository.findAllByGender("MALE")).thenReturn(new ArrayList<>());

    List<RoommateResponseDTO> result = roommateService.getAllRoommates();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(userRepository, times(1)).findByEmail("test@example.com");
    verify(roommateRepository, times(1)).findAllByGender("MALE");
  }

  @Test
  void getAllRoommates_OnlyCurrentUserRoommateExists_ReturnsEmptyList() {
    // Current user has a roommate profile
    Roommate currentUserRoommate = new Roommate();
    currentUserRoommate.setId(3L);
    currentUserRoommate.setGender("MALE");
    currentUserRoommate.setUser(currentUser);

    when(authentication.getName()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    lenient().when(roommateRepository.findAllByGender("MALE")).thenReturn(Arrays.asList(currentUserRoommate));

    List<RoommateResponseDTO> result = roommateService.getAllRoommates();

    assertNotNull(result);
    assertTrue(result.isEmpty()); // Should be filtered out

    verify(userRepository, times(1)).findByEmail("test@example.com");
    verify(roommateRepository, times(1)).findAllByGender("MALE");
  }

  // Tests for getRecommendations method
  @Test
  @SuppressWarnings("unchecked")
  void getRecommendations_SuccessfulResponse_ReturnsRecommendations() throws JsonProcessingException {
    Long userId = 1L;
    List<AiRecommendationDTO> expectedRecommendations = Arrays.asList(new AiRecommendationDTO());

    ResponseEntity<String> responseEntity = new ResponseEntity<>("[{\"user_id\": 2}]", HttpStatus.OK);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
        .thenReturn(responseEntity);
    when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
        .thenReturn(expectedRecommendations);

    List<AiRecommendationDTO> result = roommateService.getRecommendations(userId);

    assertNotNull(result);
    assertEquals(expectedRecommendations, result);

    verify(restTemplate, times(1)).exchange(
        eq("http://localhost:8000/recommend?user_id=1"),
        eq(HttpMethod.GET),
        isNull(),
        eq(String.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void getRecommendations_NoRecommendationsFound_ThrowsNotFoundException() throws JsonProcessingException {
    Long userId = 1L;
    ResponseEntity<String> responseEntity = new ResponseEntity<>("No recommendations", HttpStatus.NOT_FOUND);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
        .thenReturn(responseEntity);

    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      roommateService.getRecommendations(userId);
    });

    assertEquals("Không tìm thấy kết quả phù hợp", exception.getMessage());

    verify(restTemplate, times(1)).exchange(
        eq("http://localhost:8000/recommend?user_id=1"),
        eq(HttpMethod.GET),
        isNull(),
        eq(String.class));
    verify(objectMapper, never()).readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void getRecommendations_AiServiceError_ThrowsRuntimeException() throws JsonProcessingException {
    Long userId = 1L;
    ResponseEntity<String> responseEntity = new ResponseEntity<>("Internal Server Error",
        HttpStatus.INTERNAL_SERVER_ERROR);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
        .thenReturn(responseEntity);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      roommateService.getRecommendations(userId);
    });

    assertEquals("Failed to get recommendations from AI service: 500", exception.getMessage());

    verify(restTemplate, times(1)).exchange(
        eq("http://localhost:8000/recommend?user_id=1"),
        eq(HttpMethod.GET),
        isNull(),
        eq(String.class));
    verify(objectMapper, never()).readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class));
  }

  @Test
  void getRecommendations_JsonProcessingError_ThrowsJsonProcessingException() throws JsonProcessingException {
    Long userId = 1L;
    String aiResponseJson = "invalid json";
    ResponseEntity<String> responseEntity = new ResponseEntity<>(aiResponseJson, HttpStatus.OK);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
        .thenReturn(responseEntity);
    when(objectMapper.readValue(eq(aiResponseJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
        .thenThrow(new JsonProcessingException("Invalid JSON") {
        });

    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> {
      roommateService.getRecommendations(userId);
    });

    assertEquals("Invalid JSON", exception.getMessage());

    verify(restTemplate, times(1)).exchange(
        eq("http://localhost:8000/recommend?user_id=1"),
        eq(HttpMethod.GET),
        isNull(),
        eq(String.class));
    verify(objectMapper, times(1)).readValue(eq(aiResponseJson),
        any(com.fasterxml.jackson.core.type.TypeReference.class));
  }
}