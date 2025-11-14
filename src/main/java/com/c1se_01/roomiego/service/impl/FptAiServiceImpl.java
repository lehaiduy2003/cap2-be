package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.FptAiIdRecognitionResponse;
import com.c1se_01.roomiego.service.FptAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Slf4j
@Service
public class FptAiServiceImpl implements FptAiService {

  @Value("${fpt.ai.api.key}")
  private String apiKey;

  private final String API_URL = "https://api.fpt.ai/vision/idr/vnm";

  private final RestTemplate restTemplate;

  public FptAiServiceImpl() {
    this.restTemplate = new RestTemplate();
  }

  @Override
  public FptAiIdRecognitionResponse recognizeIdCard(String imageBase64) {
    try {
      log.debug("Received image base64 string length: {}", imageBase64 != null ? imageBase64.length() : 0);
      log.debug("Image starts with: {}",
          imageBase64 != null ? imageBase64.substring(0, Math.min(50, imageBase64.length())) : "null");

      // Remove data URI prefix if present (data:image/jpeg;base64,)
      String base64Data = imageBase64;
      if (imageBase64.contains(",")) {
        int commaIndex = imageBase64.indexOf(",");
        base64Data = imageBase64.substring(commaIndex + 1);
        log.debug("Removed data URI prefix. Original length: {}, New length: {}", imageBase64.length(),
            base64Data.length());
      }

      log.debug("Final base64 data length: {}", base64Data.length());
      log.debug("Base64 data sample (first 100 chars): {}",
          base64Data.substring(0, Math.min(100, base64Data.length())));

      // Decode base64 to byte array
      byte[] imageBytes = Base64.getDecoder().decode(base64Data);
      log.debug("Decoded image bytes length: {}", imageBytes.length);

      // Prepare headers for multipart/form-data
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.set("api-key", apiKey);

      // Create ByteArrayResource from image bytes
      ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
        @Override
        public String getFilename() {
          return "image.jpg"; // FPT.AI needs a filename
        }
      };

      // Prepare multipart request body
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("image", imageResource);

      log.debug("Multipart form data prepared with image size: {} bytes", imageBytes.length);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // Make API call
      ResponseEntity<FptAiIdRecognitionResponse> response = restTemplate.exchange(
          API_URL,
          HttpMethod.POST,
          requestEntity,
          FptAiIdRecognitionResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        FptAiIdRecognitionResponse responseBody = response.getBody();
        log.debug("FPT.AI API call successful. Error code: {}", responseBody.getErrorCode());
        log.debug("FPT.AI API response: {}", responseBody);
        return responseBody;
      } else {
        log.error("FPT.AI API returned non-OK status: {}", response.getStatusCode());
        FptAiIdRecognitionResponse errorResponse = new FptAiIdRecognitionResponse();
        errorResponse.setErrorCode(500);
        errorResponse.setErrorMessage("API returned status: " + response.getStatusCode());
        return errorResponse;
      }

    } catch (Exception e) {
      log.error("Error calling FPT.AI ID Recognition API: ", e);
      FptAiIdRecognitionResponse errorResponse = new FptAiIdRecognitionResponse();
      errorResponse.setErrorCode(500);
      errorResponse.setErrorMessage("Error calling FPT.AI API: " + e.getMessage());
      return errorResponse;
    }
  }
}
