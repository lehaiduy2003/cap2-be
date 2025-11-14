package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.FptAiIdRecognitionResponse;
import com.c1se_01.roomiego.dto.VerificationDto;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.FptAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private FptAiService fptAiService;

  @InjectMocks
  private VerificationServiceImpl verificationService;

  private User testUser;
  private VerificationDto testVerificationDto;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setFullName("Test User");
    testUser.setCitizenIdNumber(null);
    testUser.setIsVerified(false);
    testUser.setVerificationDate(null);

    testVerificationDto = new VerificationDto();
    testVerificationDto.setUserId(1L);
    testVerificationDto.setCitizenIdNumber("123456789");
  }

  @Test
  void verifyCitizenId_HappyPath_SuccessfulVerification() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByCitizenIdNumber("123456789")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("User verified successfully", result.getMessage());
    assertEquals(1L, result.getUserId());
    assertEquals("123456789", result.getCitizenIdNumber());
    assertTrue(result.getIsVerified());
    assertNotNull(result.getVerificationDate());

    verify(userRepository).findById(1L);
    verify(userRepository).findByCitizenIdNumber("123456789");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void verifyCitizenId_UserIdIsNull_Returns400Error() {
    // Given
    testVerificationDto.setUserId(null);

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("User ID is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_CitizenIdNumberIsNull_Returns400Error() {
    // Given
    testVerificationDto.setCitizenIdNumber(null);

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Citizen ID number is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_CitizenIdNumberIsEmpty_Returns400Error() {
    // Given
    testVerificationDto.setCitizenIdNumber("");

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Citizen ID number is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_CitizenIdNumberIsWhitespace_Returns400Error() {
    // Given
    testVerificationDto.setCitizenIdNumber("   ");

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Citizen ID number is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_UserNotFound_Returns404Error() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getError());

    verify(userRepository).findById(1L);
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_CitizenIdAlreadyUsedByAnotherUser_Returns409Error() {
    // Given
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setCitizenIdNumber("123456789");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByCitizenIdNumber("123456789")).thenReturn(Optional.of(anotherUser));

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(409, result.getStatusCode());
    assertEquals("This citizen ID is already registered to another user", result.getError());

    verify(userRepository).findById(1L);
    verify(userRepository).findByCitizenIdNumber("123456789");
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyCitizenId_CitizenIdAlreadyUsedBySameUser_ProceedsWithVerification() {
    // Given
    testUser.setCitizenIdNumber("123456789");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByCitizenIdNumber("123456789")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("User verified successfully", result.getMessage());

    verify(userRepository).findById(1L);
    verify(userRepository).findByCitizenIdNumber("123456789");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void verifyCitizenId_ExceptionDuringProcessing_Returns500Error() {
    // Given
    when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

    // When
    VerificationDto result = verificationService.verifyCitizenId(testVerificationDto);

    // Then
    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("Error occurred during verification"));
    assertTrue(result.getError().contains("Database error"));

    verify(userRepository).findById(1L);
    verify(userRepository, never()).findByCitizenIdNumber(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void getVerificationStatus_HappyPath_UserExists() {
    // Given
    testUser.setIsVerified(true);
    testUser.setVerificationDate(LocalDateTime.now());

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    VerificationDto result = verificationService.getVerificationStatus(1L);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("Verification status retrieved successfully", result.getMessage());
    assertEquals(1L, result.getUserId());
    assertNull(result.getCitizenIdNumber());
    assertTrue(result.getIsVerified());
    assertNotNull(result.getVerificationDate());

    verify(userRepository).findById(1L);
  }

  @Test
  void getVerificationStatus_UserIdIsNull_Returns400Error() {
    // When
    VerificationDto result = verificationService.getVerificationStatus(null);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("User ID is required", result.getError());

    verify(userRepository, never()).findById(any());
  }

  @Test
  void getVerificationStatus_UserNotFound_Returns404Error() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    VerificationDto result = verificationService.getVerificationStatus(1L);

    // Then
    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getError());

    verify(userRepository).findById(1L);
  }

  @Test
  void getVerificationStatus_UserNotVerified_ReturnsFalseStatus() {
    // Given
    testUser.setIsVerified(false);
    testUser.setVerificationDate(null);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    VerificationDto result = verificationService.getVerificationStatus(1L);

    // Then
    assertEquals(200, result.getStatusCode());
    assertFalse(result.getIsVerified());
    assertNull(result.getVerificationDate());

    verify(userRepository).findById(1L);
  }

  @Test
  void getVerificationStatus_ExceptionDuringProcessing_Returns500Error() {
    // Given
    when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

    // When
    VerificationDto result = verificationService.getVerificationStatus(1L);

    // Then
    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("Error occurred while retrieving verification status"));
    assertTrue(result.getError().contains("Database error"));

    verify(userRepository).findById(1L);
  }

  @Test
  void updateVerificationStatus_HappyPath_SetToVerified() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    VerificationDto result = verificationService.updateVerificationStatus(1L, true);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("Verification status updated successfully", result.getMessage());
    assertEquals(1L, result.getUserId());
    assertTrue(result.getIsVerified());
    assertNotNull(result.getVerificationDate());

    verify(userRepository).findById(1L);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateVerificationStatus_HappyPath_SetToUnverified() {
    // Given
    testUser.setIsVerified(true);
    testUser.setVerificationDate(LocalDateTime.now());

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    VerificationDto result = verificationService.updateVerificationStatus(1L, false);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("Verification status updated successfully", result.getMessage());
    assertEquals(1L, result.getUserId());
    assertFalse(result.getIsVerified());
    assertNull(result.getVerificationDate());

    verify(userRepository).findById(1L);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateVerificationStatus_UserIdIsNull_Returns400Error() {
    // When
    VerificationDto result = verificationService.updateVerificationStatus(null, true);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("User ID is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void updateVerificationStatus_UserNotFound_Returns404Error() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    VerificationDto result = verificationService.updateVerificationStatus(1L, true);

    // Then
    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getError());

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  void updateVerificationStatus_ExceptionDuringProcessing_Returns500Error() {
    // Given
    when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

    // When
    VerificationDto result = verificationService.updateVerificationStatus(1L, true);

    // Then
    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("Error occurred while updating verification status"));
    assertTrue(result.getError().contains("Database error"));

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyWithFptAi_HappyPath_SuccessfulVerification() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    // Mock FPT.AI responses
    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, "123456789", "John Doe", "01/01/1990",
        "Nam", "front");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null, null, null, null, null, "back");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);
    when(userRepository.findByCitizenIdNumber("123456789")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(200, result.getStatusCode());
    assertEquals("User verified successfully with both front and back ID card images", result.getMessage());
    assertEquals(1L, result.getUserId());
    assertEquals("123456789", result.getCitizenIdNumber());
    assertTrue(result.getIsVerified());
    assertNotNull(result.getVerificationDate());
    assertEquals("John Doe", result.getName());
    assertEquals("01/01/1990", result.getDateOfBirth());
    assertEquals("MALE", result.getSex());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
    verify(userRepository).findByCitizenIdNumber("123456789");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void verifyWithFptAi_UserIdIsNull_Returns400Error() {
    // Given
    testVerificationDto.setUserId(null);
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("User ID is required", result.getError());

    verify(userRepository, never()).findById(any());
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_FrontImageIsNull_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64(null);
    testVerificationDto.setBackImageBase64("backImageData");

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Front image of ID card is required for verification", result.getError());

    verify(userRepository, never()).findById(any());
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_FrontImageIsEmpty_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("");
    testVerificationDto.setBackImageBase64("backImageData");

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Front image of ID card is required for verification", result.getError());

    verify(userRepository, never()).findById(any());
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_BackImageIsNull_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64(null);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Back image of ID card is required for verification", result.getError());

    verify(userRepository, never()).findById(any());
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_BackImageIsEmpty_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("");

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Back image of ID card is required for verification", result.getError());

    verify(userRepository, never()).findById(any());
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_UserNotFound_Returns404Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  @Test
  void verifyWithFptAi_FrontApiCallFails_Returns502Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(1, "Front API error");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null); // Back succeeds

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(502, result.getStatusCode());
    assertTrue(result.getError().contains("FPT.AI verification failed"));
    assertTrue(result.getError().contains("Front image error: Front API error"));

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_BackApiCallFails_Returns502Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, "123456789");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(1, "Back API error");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(502, result.getStatusCode());
    assertTrue(result.getError().contains("FPT.AI verification failed"));
    assertTrue(result.getError().contains("Back image error: Back API error"));

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_BothApiCallsFail_Returns502Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(1, "Front API error");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(1, "Back API error");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(502, result.getStatusCode());
    assertTrue(result.getError().contains("FPT.AI verification failed"));
    assertTrue(result.getError().contains("Front image error: Front API error"));
    assertTrue(result.getError().contains("Back image error: Back API error"));

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_FrontDataIsEmpty_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null);
    frontResponse.setData(new ArrayList<>()); // Empty data list

    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("No ID card data found in the front image. Please provide a clear image.", result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_BackDataIsEmpty_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, "123456789");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null);
    backResponse.setData(new ArrayList<>()); // Empty data list

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("No ID card data found in the back image. Please provide a clear image.", result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_CitizenIdNotExtractedFromFront_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, null); // No ID
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("Could not extract citizen ID from the front image. Please provide a clear image.", result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_BackTypeNotValid_Returns400Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, "123456789");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null, null, null, null, null, "invalid_type");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(400, result.getStatusCode());
    assertEquals("The back image does not appear to be the back side of an ID card. Please provide the correct image.",
        result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
  }

  @Test
  void verifyWithFptAi_CitizenIdAlreadyUsedByAnotherUser_Returns409Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setCitizenIdNumber("123456789");

    FptAiIdRecognitionResponse frontResponse = createMockFptAiResponse(0, null, "123456789", "John Doe", "01/01/1990",
        "Nam", "front");
    FptAiIdRecognitionResponse backResponse = createMockFptAiResponse(0, null, null, null, null, null, "back");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(fptAiService.recognizeIdCard("frontImageData")).thenReturn(frontResponse);
    when(fptAiService.recognizeIdCard("backImageData")).thenReturn(backResponse);
    when(userRepository.findByCitizenIdNumber("123456789")).thenReturn(Optional.of(anotherUser));

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(409, result.getStatusCode());
    assertEquals("This citizen ID is already registered to another user", result.getError());

    verify(userRepository).findById(1L);
    verify(fptAiService).recognizeIdCard("frontImageData");
    verify(fptAiService).recognizeIdCard("backImageData");
    verify(userRepository).findByCitizenIdNumber("123456789");
    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyWithFptAi_ExceptionDuringProcessing_Returns500Error() {
    // Given
    testVerificationDto.setFrontImageBase64("frontImageData");
    testVerificationDto.setBackImageBase64("backImageData");

    when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

    // When
    VerificationDto result = verificationService.verifyWithFptAi(testVerificationDto);

    // Then
    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("Error occurred during verification"));
    assertTrue(result.getError().contains("Database error"));

    verify(userRepository).findById(1L);
    verify(fptAiService, never()).recognizeIdCard(any());
  }

  // Helper method to create mock FPT.AI responses
  private FptAiIdRecognitionResponse createMockFptAiResponse(int errorCode, String errorMessage) {
    return createMockFptAiResponse(errorCode, errorMessage, null, null, null, null, null);
  }

  private FptAiIdRecognitionResponse createMockFptAiResponse(int errorCode, String errorMessage, String id) {
    return createMockFptAiResponse(errorCode, errorMessage, id, null, null, null, null);
  }

  private FptAiIdRecognitionResponse createMockFptAiResponse(int errorCode, String errorMessage, String id, String name,
      String dob, String sex, String type) {
    FptAiIdRecognitionResponse response = new FptAiIdRecognitionResponse();
    response.setErrorCode(errorCode);
    response.setErrorMessage(errorMessage);

    if (errorCode == 0) {
      List<FptAiIdRecognitionResponse.IdCardData> dataList = new ArrayList<>();
      FptAiIdRecognitionResponse.IdCardData data = new FptAiIdRecognitionResponse.IdCardData();
      data.setId(id);
      data.setName(name);
      data.setDob(dob);
      data.setSex(sex);
      data.setType(type);
      data.setTypeNew(type);
      dataList.add(data);
      response.setData(dataList);
    }

    return response;
  }
}