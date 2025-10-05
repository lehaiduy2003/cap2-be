package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.LocationResponse;
import com.c1se_01.roomiego.dto.LocationSearchRequest;
import com.c1se_01.roomiego.service.GoogleMapsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        LocationResponse response = googleMapsService.searchLocation(request.getAddress());
        
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
