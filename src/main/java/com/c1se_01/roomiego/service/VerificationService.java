package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.VerificationDto;

public interface VerificationService {

  /**
   * Verify user's citizen ID
   * 
   * @param verificationDto Contains citizen ID information and images
   * @return VerificationDto with status and verification result
   */
  VerificationDto verifyCitizenId(VerificationDto verificationDto);

  /**
   * Get verification status for a user
   * 
   * @param userId The user ID to check
   * @return VerificationDto with current verification status
   */
  VerificationDto getVerificationStatus(Long userId);

  /**
   * Update verification status manually (for admin purposes)
   * 
   * @param userId     The user ID to update
   * @param isVerified Verification status
   * @return VerificationDto with updated status
   */
  VerificationDto updateVerificationStatus(Long userId, Boolean isVerified);

  /**
   * Verify user's citizen ID using FPT.AI ID Recognition service
   * 
   * @param verificationDto Contains user ID and base64 encoded ID card image
   * @return VerificationDto with extracted information and verification result
   */
  VerificationDto verifyWithFptAi(VerificationDto verificationDto);
}
