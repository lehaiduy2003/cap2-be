package com.c1se_01.roomiego.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationDto {

  private Long userId;
  private String citizenIdNumber;
  private Boolean isVerified;
  private LocalDateTime verificationDate;

  // For FPT.AI ID recognition - both front and back
  private String frontImageBase64;
  private String backImageBase64;

  // Response fields
  private int statusCode;
  private String message;
  private String error;

  // Extracted data from FPT.AI (front side)
  private String name;
  private String dateOfBirth;
  private String sex;
  private String nationality;
  private String placeOfOrigin;
  private String placeOfResidence;
  private String expiryDate;

  // Additional data from back side
  private String issueDate;
  private String features;
}
