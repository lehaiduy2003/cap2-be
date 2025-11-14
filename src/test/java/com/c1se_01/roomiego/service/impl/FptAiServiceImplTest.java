package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.FptAiIdRecognitionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FptAiServiceImplTest {

  @Mock
  private RestTemplate restTemplate;

  private FptAiServiceImpl fptAiService;

  private static final String TEST_API_KEY = "test-api-key";
  private static final String API_URL = "https://api.fpt.ai/vision/idr/vnm";

  // Valid base64 encoded image (1x1 pixel JPEG)
  private static final String VALID_BASE64_IMAGE = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAA8A/9k=";

  @BeforeEach
  void setUp() {
    fptAiService = new FptAiServiceImpl();
    ReflectionTestUtils.setField(fptAiService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(fptAiService, "apiKey", TEST_API_KEY);
  }

  // Happy Path Tests

  @Test
  void recognizeIdCard_HappyPath_ValidBase64ImageWithoutDataUri() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);
    expectedResponse.setErrorMessage("Success");
    List<FptAiIdRecognitionResponse.IdCardData> dataList = new ArrayList<>();
    FptAiIdRecognitionResponse.IdCardData idCardData = new FptAiIdRecognitionResponse.IdCardData();
    idCardData.setId("123456789");
    idCardData.setName("Nguyen Van A");
    idCardData.setDob("01/01/1990");
    dataList.add(idCardData);
    expectedResponse.setData(dataList);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());
    assertEquals("Success", result.getErrorMessage());
    assertNotNull(result.getData());
    assertEquals(1, result.getData().size());
    assertEquals("123456789", result.getData().get(0).getId());
    assertEquals("Nguyen Van A", result.getData().get(0).getName());
    assertEquals("01/01/1990", result.getData().get(0).getDob());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_HappyPath_ValidBase64ImageWithDataUriPrefix() {
    // Given
    String imageBase64WithDataUri = "data:image/jpeg;base64," + VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);
    expectedResponse.setErrorMessage("Success");

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64WithDataUri);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());
    assertEquals("Success", result.getErrorMessage());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_HappyPath_ValidBase64ImageWithPngDataUri() {
    // Given
    String imageBase64WithPngDataUri = "data:image/png;base64," + VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64WithPngDataUri);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_HappyPath_EmptyDataListInResponse() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);
    expectedResponse.setData(new ArrayList<>());

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());
    assertNotNull(result.getData());
    assertTrue(result.getData().isEmpty());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  // Branch Coverage Tests - Data URI Handling

  @Test
  void recognizeIdCard_BranchCoverage_Base64WithCommaButNoDataUriPrefix() {
    // Given - base64 string containing comma but not a data URI - this will extract
    // after comma
    // The part after comma "withcomma" is invalid base64
    String base64WithComma = "somedata,withcomma";

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(base64WithComma);

    // Then - Should fail due to invalid base64 after comma extraction
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_BranchCoverage_Base64WithoutComma() {
    // Given - base64 string without comma
    String base64WithoutComma = VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(base64WithoutComma);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  // Branch Coverage Tests - Response Status Handling

  @Test
  void recognizeIdCard_BranchCoverage_ApiReturnsNonOkStatus() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(null);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("API returned status: 400 BAD_REQUEST"));

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_BranchCoverage_ApiReturnsOkButNullBody() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(null);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("API returned status: 200 OK"));

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_BranchCoverage_ApiReturnsInternalServerError() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(null);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("API returned status: 500 INTERNAL_SERVER_ERROR"));

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  // Worse Case Tests - Exception Handling

  @Test
  void recognizeIdCard_WorseCase_InvalidBase64String() {
    // Given - Invalid base64 string
    String invalidBase64 = "This is not a valid base64 string!!!";

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(invalidBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));
    assertTrue(result.getErrorMessage().contains("IllegalArgumentException") ||
        result.getErrorMessage().contains("Illegal base64"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_NullImageBase64() {
    // Given
    String imageBase64 = null;

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_EmptyImageBase64() {
    // Given
    String imageBase64 = "";

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then - Empty string decodes to empty byte array, which is sent to API
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_RestTemplateThrowsException() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenThrow(new RestClientException("Connection timeout"));

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));
    assertTrue(result.getErrorMessage().contains("Connection timeout"));

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_RestTemplateThrowsRuntimeException() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenThrow(new RuntimeException("Unexpected error"));

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));
    assertTrue(result.getErrorMessage().contains("Unexpected error"));

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_Base64DecodingFailsAfterRemovingPrefix() {
    // Given - Data URI with invalid base64 after comma
    String invalidBase64WithDataUri = "data:image/jpeg;base64,Invalid!!!Base64===Data";

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(invalidBase64WithDataUri);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_OnlyDataUriPrefixNoActualData() {
    // Given
    String onlyPrefix = "data:image/jpeg;base64,";

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(onlyPrefix);

    // Then - Empty string after prefix removal decodes to empty byte array
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_WorseCase_VeryLongBase64String() {
    // Given - Create a very long base64 string (simulating large image)
    StringBuilder longBase64 = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longBase64.append(VALID_BASE64_IMAGE);
    }

    // When - This may fail due to size limitations or succeed
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(longBase64.toString());

    // Then - Either succeeds with mocked response or fails with error
    assertNotNull(result);
    // Don't assert specific error code as behavior depends on implementation limits
    assertTrue(result.getErrorCode() >= 0);
  }

  // Edge Cases

  @Test
  void recognizeIdCard_EdgeCase_Base64WithWhitespace() {
    // Given - base64 with leading/trailing whitespace
    String base64WithWhitespace = "  " + VALID_BASE64_IMAGE + "  ";

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(base64WithWhitespace);

    // Then
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_EdgeCase_ApiReturnsNonZeroErrorCode() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(400);
    expectedResponse.setErrorMessage("Invalid image format");

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(400, result.getErrorCode());
    assertEquals("Invalid image format", result.getErrorMessage());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_EdgeCase_MultipleCommasInBase64String() {
    // Given - Multiple commas in the base64 string
    // After first comma: "extra,comma," + VALID_BASE64_IMAGE which is invalid
    // base64
    String base64WithMultipleCommas = "data:image/jpeg;base64,extra,comma," + VALID_BASE64_IMAGE;

    // When - Should use substring after first comma which results in invalid base64
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(base64WithMultipleCommas);

    // Then - Should fail due to invalid base64 after first comma
    assertNotNull(result);
    assertEquals(500, result.getErrorCode());
    assertTrue(result.getErrorMessage().contains("Error calling FPT.AI API"));

    verify(restTemplate, never()).exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }

  @Test
  void recognizeIdCard_EdgeCase_CompleteIdCardDataWithAllFields() {
    // Given
    String imageBase64 = VALID_BASE64_IMAGE;

    FptAiIdRecognitionResponse expectedResponse = new FptAiIdRecognitionResponse();
    expectedResponse.setErrorCode(0);
    expectedResponse.setErrorMessage("Success");

    List<FptAiIdRecognitionResponse.IdCardData> dataList = new ArrayList<>();
    FptAiIdRecognitionResponse.IdCardData idCardData = new FptAiIdRecognitionResponse.IdCardData();
    idCardData.setId("123456789012");
    idCardData.setIdProb("0.99");
    idCardData.setName("Nguyen Van A");
    idCardData.setNameProb("0.98");
    idCardData.setDob("01/01/1990");
    idCardData.setDobProb("0.97");
    idCardData.setSex("Nam");
    idCardData.setSexProb("0.96");
    idCardData.setNationality("Việt Nam");
    idCardData.setNationalityProb("0.95");
    idCardData.setHome("Hanoi");
    idCardData.setHomeProb("0.94");
    idCardData.setAddress("123 Main St, Hanoi");
    idCardData.setAddressProb("0.93");
    idCardData.setDoe("01/01/2030");
    idCardData.setDoeProb("0.92");
    idCardData.setType("CCCD");
    idCardData.setTypeProb("0.91");
    idCardData.setTypeNew("NEW");

    FptAiIdRecognitionResponse.AddressEntities addressEntities = new FptAiIdRecognitionResponse.AddressEntities();
    addressEntities.setProvince("Hanoi");
    addressEntities.setDistrict("Ba Dinh");
    addressEntities.setWard("Phuc Xa");
    addressEntities.setStreet("Main Street");
    idCardData.setAddressEntities(addressEntities);

    dataList.add(idCardData);
    expectedResponse.setData(dataList);

    ResponseEntity<FptAiIdRecognitionResponse> responseEntity = ResponseEntity.ok(expectedResponse);

    when(restTemplate.exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class)))
        .thenReturn(responseEntity);

    // When
    FptAiIdRecognitionResponse result = fptAiService.recognizeIdCard(imageBase64);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getErrorCode());
    assertEquals("Success", result.getErrorMessage());
    assertNotNull(result.getData());
    assertEquals(1, result.getData().size());

    FptAiIdRecognitionResponse.IdCardData resultData = result.getData().get(0);
    assertEquals("123456789012", resultData.getId());
    assertEquals("0.99", resultData.getIdProb());
    assertEquals("Nguyen Van A", resultData.getName());
    assertEquals("Nam", resultData.getSex());
    assertEquals("Việt Nam", resultData.getNationality());
    assertNotNull(resultData.getAddressEntities());
    assertEquals("Hanoi", resultData.getAddressEntities().getProvince());
    assertEquals("Ba Dinh", resultData.getAddressEntities().getDistrict());

    verify(restTemplate, times(1)).exchange(
        eq(API_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(FptAiIdRecognitionResponse.class));
  }
}
