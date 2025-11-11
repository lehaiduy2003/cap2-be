package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.LocationMarkerRequest;
import com.c1se_01.roomiego.dto.LocationMarkerResponse;
import com.c1se_01.roomiego.dto.LocationResponse;
import com.c1se_01.roomiego.dto.LocationSearchRequest;
import com.c1se_01.roomiego.service.impl.GoogleMapsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/maps")
@RequiredArgsConstructor
public class MapsController {

    private final GoogleMapsService googleMapsService;

    @PostMapping("/locations")
    public ResponseEntity<LocationResponse> searchLocation(@RequestBody LocationSearchRequest request) {
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            LocationResponse errorResponse = new LocationResponse(
                    "ERROR",
                    "Address is required",
                    null,
                    null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        LocationResponse response = googleMapsService.searchLocation(request.getAddress());

        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/markers")
    public ResponseEntity<LocationMarkerResponse[]> getMarkers(@RequestBody LocationMarkerRequest[] request) {
        if (request.length == 0) {
            return ResponseEntity.badRequest().body(new LocationMarkerResponse[0]);
        }

        LocationMarkerResponse[] responses = googleMapsService.getMarkers(Arrays.stream(request).toList());
        return ResponseEntity.ok(responses);
    }
}
