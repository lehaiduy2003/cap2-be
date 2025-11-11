package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.LocationMarkerRequest;
import com.c1se_01.roomiego.dto.LocationMarkerResponse;
import com.c1se_01.roomiego.dto.LocationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleMapsServiceTest {

  @Mock
  private RestTemplate restTemplate;

  private GoogleMapsService googleMapsService;

  @BeforeEach
  void setUp() {
    // Create service instance with mocked dependencies
    googleMapsService = new GoogleMapsService(restTemplate, new ObjectMapper());

    // Set the API key via reflection
    ReflectionTestUtils.setField(googleMapsService, "googleMapsApiKey", "test-api-key");
  }

  // Tests for getMarkers method
  @Test
  void getMarkers_HappyPath_AllAddressesGeocodeSuccessfully() {
    // Given
    List<LocationMarkerRequest> requests = Arrays.asList(
        new LocationMarkerRequest("123 Main St, Hanoi", 1),
        new LocationMarkerRequest("456 Oak Ave, Ho Chi Minh City", 2));

    try {
      // Mock successful geocoding responses - different responses for each address
      String jsonResponse1 = "{\"status\":\"OK\",\"results\":[{\"formatted_address\":\"123 Main St, Hanoi, Vietnam\",\"geometry\":{\"location\":{\"lat\":21.0285,\"lng\":105.8542}},\"place_id\":\"place1\"}]}";
      String jsonResponse2 = "{\"status\":\"OK\",\"results\":[{\"formatted_address\":\"456 Oak Ave, Ho Chi Minh City, Vietnam\",\"geometry\":{\"location\":{\"lat\":10.8231,\"lng\":106.6297}},\"place_id\":\"place2\"}]}";

      ResponseEntity<String> responseEntity1 = ResponseEntity.ok(jsonResponse1);
      ResponseEntity<String> responseEntity2 = ResponseEntity.ok(jsonResponse2);

      when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
          .thenReturn(responseEntity1)
          .thenReturn(responseEntity2);
    } catch (Exception e) {
      fail("Failed to setup mocks: " + e.getMessage());
    }

    // When
    LocationMarkerResponse[] result = googleMapsService.getMarkers(requests);

    // Then
    assertEquals(2, result.length);
    assertEquals(1, result[0].getId());
    assertEquals("123 Main St, Hanoi, Vietnam", result[0].getAddress());
    assertEquals(105.8542, result[0].getLongitude());
    assertEquals(21.0285, result[0].getLatitude());

    assertEquals(2, result[1].getId());
    assertEquals("456 Oak Ave, Ho Chi Minh City, Vietnam", result[1].getAddress());
    assertEquals(106.6297, result[1].getLongitude());
    assertEquals(10.8231, result[1].getLatitude());
  }

  @Test
  void getMarkers_PartialGeocodingFailures_ReturnsOnlySuccessfulResults() {
    // Given
    List<LocationMarkerRequest> requests = Arrays.asList(
        new LocationMarkerRequest("123 Main St, Hanoi", 1),
        new LocationMarkerRequest("Invalid Address", 2),
        new LocationMarkerRequest("456 Oak Ave, Ho Chi Minh City", 3));

    try {
      // Mock first successful, second fails (all variations), third successful
      String jsonResponse1 = "{\"status\":\"OK\",\"results\":[{\"formatted_address\":\"123 Main St, Hanoi, Vietnam\",\"geometry\":{\"location\":{\"lat\":21.0285,\"lng\":105.8542}},\"place_id\":\"place1\"}]}";
      String jsonFailure = "{\"status\":\"ZERO_RESULTS\",\"results\":[]}";
      String jsonResponse3 = "{\"status\":\"OK\",\"results\":[{\"formatted_address\":\"456 Oak Ave, Ho Chi Minh City, Vietnam\",\"geometry\":{\"location\":{\"lat\":10.8231,\"lng\":106.6297}},\"place_id\":\"place3\"}]}";

      ResponseEntity<String> responseEntity1 = ResponseEntity.ok(jsonResponse1);
      ResponseEntity<String> responseEntityFailure = ResponseEntity.ok(jsonFailure);
      ResponseEntity<String> responseEntity3 = ResponseEntity.ok(jsonResponse3);

      when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
          .thenReturn(responseEntity1) // First address succeeds
          .thenReturn(responseEntityFailure) // Invalid address fails
          .thenReturn(responseEntityFailure) // Invalid + Vietnam fails
          .thenReturn(responseEntityFailure) // Invalid + Việt Nam fails
          .thenReturn(responseEntityFailure) // Invalid with Đường fails
          .thenReturn(responseEntity3); // Third address succeeds
    } catch (Exception e) {
      fail("Failed to setup mocks: " + e.getMessage());
    }

    // When
    LocationMarkerResponse[] result = googleMapsService.getMarkers(requests);

    // Then
    assertEquals(2, result.length);
    assertEquals(1, result[0].getId());
    assertEquals(3, result[1].getId());
  }

  @Test
  void getMarkers_AllGeocodingFailures_ReturnsEmptyArray() {
    // Given
    List<LocationMarkerRequest> requests = Arrays.asList(
        new LocationMarkerRequest("Invalid Address 1", 1),
        new LocationMarkerRequest("Invalid Address 2", 2));

    try {
      // Mock all geocoding failures
      String jsonFailure = "{\"status\":\"ZERO_RESULTS\",\"results\":[]}";
      ResponseEntity<String> responseEntityFailure = ResponseEntity.ok(jsonFailure);

      when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
          .thenReturn(responseEntityFailure);
    } catch (Exception e) {
      fail("Failed to setup mocks: " + e.getMessage());
    }

    // When
    LocationMarkerResponse[] result = googleMapsService.getMarkers(requests);

    // Then
    assertEquals(0, result.length);
  }

  // Tests for searchLocation method
  @Test
  void searchLocation_HappyPath_AddressFoundWithNearbyPlaces() throws Exception {
    // Given
    String address = "123 Main St, Hanoi";

    try {
      // Mock geocoding success
      String jsonGeocodeResponse = "{\"status\":\"OK\",\"results\":[{\"formatted_address\":\"123 Main St, Hanoi, Vietnam\",\"geometry\":{\"location\":{\"lat\":21.0285,\"lng\":105.8542}},\"place_id\":\"place1\"}]}";
      ResponseEntity<String> geocodeResponseEntity = ResponseEntity.ok(jsonGeocodeResponse);

      // Mock nearby places search
      mockNearbyPlacesResponse(21.0285, 105.8542);

      when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
          .thenReturn(geocodeResponseEntity);
    } catch (Exception e) {
      fail("Failed to setup mocks: " + e.getMessage());
    }

    // When
    LocationResponse result = googleMapsService.searchLocation(address);

    // Then
    assertEquals("SUCCESS", result.getStatus());
    assertEquals("Location found successfully", result.getMessage());
    assertNotNull(result.getLocation());
    assertEquals("123 Main St, Hanoi, Vietnam", result.getLocation().getFormattedAddress());
    assertEquals(21.0285, result.getLocation().getLatitude());
    assertEquals(105.8542, result.getLocation().getLongitude());
    assertNotNull(result.getNearbyPlaces());
    assertFalse(result.getNearbyPlaces().isEmpty());
  }

  @Test
  void searchLocation_AddressNotFound_ReturnsErrorResponse() {
    // Given
    String address = "Invalid Address";

    try {
      // Mock geocoding failure for all variations
      String jsonFailure = "{\"status\":\"ZERO_RESULTS\",\"results\":[]}";
      ResponseEntity<String> responseEntityFailure = ResponseEntity.ok(jsonFailure);

      when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
          .thenReturn(responseEntityFailure);
    } catch (Exception e) {
      fail("Failed to setup mocks: " + e.getMessage());
    }

    // When
    LocationResponse result = googleMapsService.searchLocation(address);

    // Then
    assertEquals("ERROR", result.getStatus());
    assertEquals("Address not found", result.getMessage());
    assertNull(result.getLocation());
    assertNull(result.getNearbyPlaces());
  }

  // Helper methods for mocking
  private void mockNearbyPlacesResponse(double lat, double lng) {
    String jsonResponse = String.format(
        "{\"status\":\"OK\",\"results\":[" +
            "{\"name\":\"Hospital A\",\"geometry\":{\"location\":{\"lat\":%f,\"lng\":%f}},\"place_id\":\"place1\",\"rating\":4.5,\"vicinity\":\"Near location\"},"
            +
            "{\"name\":\"School B\",\"geometry\":{\"location\":{\"lat\":%f,\"lng\":%f}},\"place_id\":\"place2\",\"rating\":4.0}"
            +
            "]}",
        lat + 0.01, lng + 0.01, lat + 0.02, lng + 0.02);

    when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);
  }
}