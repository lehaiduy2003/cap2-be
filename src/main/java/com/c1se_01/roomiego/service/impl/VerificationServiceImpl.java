package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.FptAiIdRecognitionResponse;
import com.c1se_01.roomiego.dto.VerificationDto;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.FptAiService;
import com.c1se_01.roomiego.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class VerificationServiceImpl implements VerificationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FptAiService fptAiService;

  @Override
  @Transactional
  public VerificationDto verifyCitizenId(VerificationDto verificationDto) {
    VerificationDto response = new VerificationDto();

    try {
      // Validate input
      if (verificationDto.getUserId() == null) {
        response.setStatusCode(400);
        response.setError("User ID is required");
        return response;
      }

      if (verificationDto.getCitizenIdNumber() == null || verificationDto.getCitizenIdNumber().trim().isEmpty()) {
        response.setStatusCode(400);
        response.setError("Citizen ID number is required");
        return response;
      }

      // Find user
      Optional<User> userOptional = userRepository.findById(verificationDto.getUserId());
      if (userOptional.isEmpty()) {
        response.setStatusCode(404);
        response.setError("User not found");
        return response;
      }

      User user = userOptional.get();

      // Check if citizen ID is already used by another user
      Optional<User> existingUser = userRepository.findByCitizenIdNumber(verificationDto.getCitizenIdNumber());
      if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
        response.setStatusCode(409);
        response.setError("This citizen ID is already registered to another user");
        return response;
      }

      // Update user verification information
      user.setCitizenIdNumber(verificationDto.getCitizenIdNumber());
      user.setIsVerified(true);
      user.setVerificationDate(LocalDateTime.now());

      // Save updated user
      User savedUser = userRepository.save(user);

      // Prepare response
      response.setUserId(savedUser.getId());
      response.setCitizenIdNumber(savedUser.getCitizenIdNumber());
      response.setIsVerified(savedUser.getIsVerified());
      response.setVerificationDate(savedUser.getVerificationDate());
      response.setStatusCode(200);
      response.setMessage("User verified successfully");

      log.info("User {} verified successfully with citizen ID: {}", savedUser.getId(), savedUser.getCitizenIdNumber());

    } catch (Exception e) {
      log.error("Error during citizen ID verification: ", e);
      response.setStatusCode(500);
      response.setError("Error occurred during verification: " + e.getMessage());
    }

    return response;
  }

  @Override
  public VerificationDto getVerificationStatus(Long userId) {
    VerificationDto response = new VerificationDto();

    try {
      if (userId == null) {
        response.setStatusCode(400);
        response.setError("User ID is required");
        return response;
      }

      Optional<User> userOptional = userRepository.findById(userId);
      if (userOptional.isEmpty()) {
        response.setStatusCode(404);
        response.setError("User not found");
        return response;
      }

      User user = userOptional.get();

      response.setUserId(user.getId());
      response.setCitizenIdNumber(user.getCitizenIdNumber());
      response.setIsVerified(user.getIsVerified() != null ? user.getIsVerified() : false);
      response.setVerificationDate(user.getVerificationDate());
      response.setStatusCode(200);
      response.setMessage("Verification status retrieved successfully");

    } catch (Exception e) {
      log.error("Error retrieving verification status: ", e);
      response.setStatusCode(500);
      response.setError("Error occurred while retrieving verification status: " + e.getMessage());
    }

    return response;
  }

  @Override
  @Transactional
  public VerificationDto updateVerificationStatus(Long userId, Boolean isVerified) {
    VerificationDto response = new VerificationDto();

    try {
      if (userId == null) {
        response.setStatusCode(400);
        response.setError("User ID is required");
        return response;
      }

      Optional<User> userOptional = userRepository.findById(userId);
      if (userOptional.isEmpty()) {
        response.setStatusCode(404);
        response.setError("User not found");
        return response;
      }

      User user = userOptional.get();
      user.setIsVerified(isVerified);

      if (isVerified && user.getVerificationDate() == null) {
        user.setVerificationDate(LocalDateTime.now());
      } else if (!isVerified) {
        user.setVerificationDate(null);
      }

      User savedUser = userRepository.save(user);

      response.setUserId(savedUser.getId());
      response.setIsVerified(savedUser.getIsVerified());
      response.setVerificationDate(savedUser.getVerificationDate());
      response.setStatusCode(200);
      response.setMessage("Verification status updated successfully");

      log.info("User {} verification status updated to: {}", savedUser.getId(), isVerified);

    } catch (Exception e) {
      log.error("Error updating verification status: ", e);
      response.setStatusCode(500);
      response.setError("Error occurred while updating verification status: " + e.getMessage());
    }

    return response;
  }

  @Override
  @Transactional
  public VerificationDto verifyWithFptAi(VerificationDto verificationDto) {
    VerificationDto response = new VerificationDto();

    try {
      // Validate input
      if (verificationDto.getUserId() == null) {
        response.setStatusCode(400);
        response.setError("User ID is required");
        return response;
      }

      if (verificationDto.getFrontImageBase64() == null || verificationDto.getFrontImageBase64().trim().isEmpty()) {
        response.setStatusCode(400);
        response.setError("Front image of ID card is required for verification");
        log.error("Front image is null or empty");
        return response;
      }

      if (verificationDto.getBackImageBase64() == null || verificationDto.getBackImageBase64().trim().isEmpty()) {
        response.setStatusCode(400);
        response.setError("Back image of ID card is required for verification");
        log.error("Back image is null or empty");
        return response;
      }

      log.debug("Received front image length: {}", verificationDto.getFrontImageBase64().length());
      log.debug("Received back image length: {}", verificationDto.getBackImageBase64().length());
      log.debug("Front image starts with: {}", verificationDto.getFrontImageBase64().substring(0,
          Math.min(50, verificationDto.getFrontImageBase64().length())));
      log.debug("Back image starts with: {}", verificationDto.getBackImageBase64().substring(0,
          Math.min(50, verificationDto.getBackImageBase64().length())));

      // Find user
      Optional<User> userOptional = userRepository.findById(verificationDto.getUserId());
      if (userOptional.isEmpty()) {
        response.setStatusCode(404);
        response.setError("User not found");
        return response;
      }

      User user = userOptional.get();

      // Call FPT.AI API to recognize FRONT side of ID card
      log.debug("Calling FPT.AI service for front image of user {}. Image size: {} chars",
          user.getId(), verificationDto.getFrontImageBase64().length());
      FptAiIdRecognitionResponse frontResponse = fptAiService.recognizeIdCard(verificationDto.getFrontImageBase64());

      // Call FPT.AI API to recognize BACK side of ID card
      log.debug("Calling FPT.AI service for back image of user {}. Image size: {} chars",
          user.getId(), verificationDto.getBackImageBase64().length());
      FptAiIdRecognitionResponse backResponse = fptAiService.recognizeIdCard(verificationDto.getBackImageBase64());

      // Check if both API calls were successful
      if (frontResponse.getErrorCode() != null && frontResponse.getErrorCode() == 0 &&
          backResponse.getErrorCode() != null && backResponse.getErrorCode() == 0) {

        // Validate both sides have data
        if (frontResponse.getData() == null || frontResponse.getData().isEmpty()) {
          response.setStatusCode(400);
          response.setError("No ID card data found in the front image. Please provide a clear image.");
          return response;
        }

        if (backResponse.getData() == null || backResponse.getData().isEmpty()) {
          response.setStatusCode(400);
          response.setError("No ID card data found in the back image. Please provide a clear image.");
          return response;
        }

        // Extract data from both sides
        FptAiIdRecognitionResponse.IdCardData frontData = frontResponse.getData().get(0);
        FptAiIdRecognitionResponse.IdCardData backData = backResponse.getData().get(0);

        // Extract citizen ID number from front (back side doesn't contain ID number)
        String citizenIdNumber = frontData.getId();

        // Validate that citizen ID was extracted from front image
        if (citizenIdNumber == null || citizenIdNumber.trim().isEmpty()) {
          response.setStatusCode(400);
          response.setError("Could not extract citizen ID from the front image. Please provide a clear image.");
          return response;
        }

        // Validate that back side is recognized as an ID card back
        String backType = backData.getTypeNew() != null ? backData.getTypeNew() : backData.getType();
        if (backType == null || (!backType.contains("back") && !backType.contains("chip_back"))) {
          response.setStatusCode(400);
          response.setError(
              "The back image does not appear to be the back side of an ID card. Please provide the correct image.");
          return response;
        }

        // Check if citizen ID is already used by another user
        Optional<User> existingUser = userRepository.findByCitizenIdNumber(citizenIdNumber);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
          response.setStatusCode(409);
          response.setError("This citizen ID is already registered to another user");
          return response;
        }

        // Update user verification information
        user.setCitizenIdNumber(citizenIdNumber);
        user.setIsVerified(true);
        user.setVerificationDate(LocalDateTime.now());

        // Update user profile with information extracted from ID card
        if (frontData.getName() != null && !frontData.getName().trim().isEmpty()) {
          user.setFullName(frontData.getName().trim());
          log.debug("Updated user full name to: {}", frontData.getName());
        }

        // Parse and update date of birth (format: DD/MM/YYYY from FPT.AI)
        if (frontData.getDob() != null && !frontData.getDob().trim().isEmpty()) {
          try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.util.Date parsedDate = inputFormat.parse(frontData.getDob());
            user.setDob(parsedDate);
            log.debug("Updated user date of birth to: {}", frontData.getDob());
          } catch (Exception e) {
            log.warn("Could not parse date of birth: {}. Error: {}", frontData.getDob(), e.getMessage());
          }
        }

        // Update gender (map Vietnamese to enum)
        if (frontData.getSex() != null && !frontData.getSex().trim().isEmpty()) {
          String gender = frontData.getSex().trim().toUpperCase();
          try {
            if (gender.contains("NAM")) {
              user.setGender(com.c1se_01.roomiego.enums.Gender.MALE);
              log.debug("Updated user gender to MALE");
            } else if (gender.contains("NỮ") || gender.contains("NU")) {
              user.setGender(com.c1se_01.roomiego.enums.Gender.FEMALE);
              log.debug("Updated user gender to FEMALE");
            }
          } catch (Exception e) {
            log.warn("Could not parse gender: {}. Error: {}", frontData.getSex(), e.getMessage());
          }
        }

        // Save updated user
        User savedUser = userRepository.save(user);
        log.info("Updated user profile with ID card information: name={}, dob={}, gender={}",
            savedUser.getFullName(), savedUser.getDob(), savedUser.getGender());

        // Prepare response with extracted data
        response.setUserId(savedUser.getId());
        response.setCitizenIdNumber(savedUser.getCitizenIdNumber());
        response.setIsVerified(savedUser.getIsVerified());
        response.setVerificationDate(savedUser.getVerificationDate());

        // Add extracted information from FPT.AI (front side)
        response.setName(savedUser.getFullName()); // Use updated user name
        response.setDateOfBirth(frontData.getDob());
        response.setSex(savedUser.getGender() != null ? savedUser.getGender().name() : frontData.getSex()); // Use
                                                                                                            // updated
                                                                                                            // gender
        response.setNationality(frontData.getNationality());
        response.setPlaceOfOrigin(frontData.getHome());
        response.setPlaceOfResidence(frontData.getAddress());
        response.setExpiryDate(frontData.getDoe());

        // Add information from back side if available
        if (backData.getDoe() != null) {
          response.setIssueDate(backData.getDoe());
        }

        response.setStatusCode(200);
        response.setMessage("User verified successfully with both front and back ID card images");

        log.info("User {} verified successfully with FPT.AI. Citizen ID: {} (front and back matched)",
            savedUser.getId(), savedUser.getCitizenIdNumber());

      } else {
        // One or both API calls failed
        StringBuilder errorMessage = new StringBuilder();

        if (frontResponse.getErrorCode() == null || frontResponse.getErrorCode() != 0) {
          errorMessage.append("Front image error: ")
              .append(frontResponse.getErrorMessage() != null ? frontResponse.getErrorMessage() : "Unknown error");
        }

        if (backResponse.getErrorCode() == null || backResponse.getErrorCode() != 0) {
          if (errorMessage.length() > 0) {
            errorMessage.append(" | ");
          }
          errorMessage.append("Back image error: ")
              .append(backResponse.getErrorMessage() != null ? backResponse.getErrorMessage() : "Unknown error");
        }

        response.setStatusCode(502);
        response.setError("FPT.AI verification failed: " + errorMessage.toString());
        log.error("FPT.AI API error for user {}: {}", user.getId(), errorMessage.toString());
      }

    } catch (Exception e) {
      log.error("Error during FPT.AI verification: ", e);
      response.setStatusCode(500);
      response.setError("Error occurred during verification: " + e.getMessage());
    }

    return response;
  }
}
