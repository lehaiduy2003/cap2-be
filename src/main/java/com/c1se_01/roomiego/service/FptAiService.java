package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.FptAiIdRecognitionResponse;

public interface FptAiService {

  /**
   * Call FPT.AI ID Recognition API to extract information from ID card image
   * 
   * @param imageBase64 Base64 encoded image of the ID card
   * @return FptAiIdRecognitionResponse containing extracted information
   */
  FptAiIdRecognitionResponse recognizeIdCard(String imageBase64);
}
