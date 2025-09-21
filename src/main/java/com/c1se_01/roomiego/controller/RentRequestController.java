package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.RentRequestCreateRequest;
import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.dto.RentRequestUpdateRequest;
import com.c1se_01.roomiego.service.RentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent-requests")
@RequiredArgsConstructor
public class RentRequestController {
    private final RentRequestService rentRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RENTER')")
    public ResponseEntity<RentRequestResponse> createRentRequest(@RequestBody RentRequestCreateRequest request) {
        RentRequestResponse response = rentRequestService.createRentRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<RentRequestResponse>> getRequestsByOwner() {
        List<RentRequestResponse> requests = rentRequestService.getRequestsByOwner();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<RentRequestResponse> updateRentRequestStatus(
            @PathVariable Long requestId,
            @RequestBody RentRequestUpdateRequest updateRequest) {
        RentRequestResponse response = rentRequestService.updateRentRequestStatus(requestId, updateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<RentRequestResponse> cancelRental(@PathVariable Long requestId) {
        RentRequestResponse response = rentRequestService.cancelRental(requestId);
        return ResponseEntity.ok(response);
    }
}
