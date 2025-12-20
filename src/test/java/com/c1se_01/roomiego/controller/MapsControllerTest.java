package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.LocationMarkerRequest;
import com.c1se_01.roomiego.dto.LocationMarkerResponse;
import com.c1se_01.roomiego.dto.LocationResponse;
import com.c1se_01.roomiego.dto.LocationSearchRequest;
import com.c1se_01.roomiego.service.impl.GoogleMapsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MapsControllerTest {

    @Mock
    private GoogleMapsService googleMapsService;

    @InjectMocks
    private MapsController mapsController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mapsController).build();
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSearchLocation_HappyCase() throws Exception {
        // Arrange
        LocationSearchRequest request = new LocationSearchRequest();
        request.setAddress("123 Main St");

        LocationResponse mockResponse = new LocationResponse("OK", "Success", null, null);

        when(googleMapsService.searchLocation("123 Main St")).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/maps/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    public void testSearchLocation_NullAddress() throws Exception {
        // Arrange
        LocationSearchRequest request = new LocationSearchRequest();
        request.setAddress(null);

        // Act & Assert
        mockMvc.perform(post("/maps/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Address is required"));
    }

    @Test
    public void testSearchLocation_EmptyAddress() throws Exception {
        // Arrange
        LocationSearchRequest request = new LocationSearchRequest();
        request.setAddress("");

        // Act & Assert
        mockMvc.perform(post("/maps/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Address is required"));
    }

    @Test
    public void testSearchLocation_WhitespaceAddress() throws Exception {
        // Arrange
        LocationSearchRequest request = new LocationSearchRequest();
        request.setAddress("   ");

        // Act & Assert
        mockMvc.perform(post("/maps/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Address is required"));
    }

    @Test
    public void testSearchLocation_ServiceError() throws Exception {
        // Arrange
        LocationSearchRequest request = new LocationSearchRequest();
        request.setAddress("Invalid Address");

        LocationResponse mockResponse = new LocationResponse("ERROR", "Location not found", null, null);

        when(googleMapsService.searchLocation("Invalid Address")).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/maps/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Location not found"));
    }

    @Test
    public void testGetMarkers_HappyCase() throws Exception {
        // Arrange
        LocationMarkerRequest[] requests = new LocationMarkerRequest[1];
        requests[0] = new LocationMarkerRequest("123 Main St", 1);

        LocationMarkerResponse[] mockResponses = new LocationMarkerResponse[1];
        mockResponses[0] = new LocationMarkerResponse(1L, "123 Main St", -74.0060, 40.7128);

        when(googleMapsService.getMarkers(anyList())).thenReturn(mockResponses);

        // Act & Assert
        mockMvc.perform(post("/maps/markers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void testGetMarkers_EmptyArray() throws Exception {
        // Arrange
        LocationMarkerRequest[] requests = new LocationMarkerRequest[0];

        // Act & Assert
        mockMvc.perform(post("/maps/markers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(0));
    }
}