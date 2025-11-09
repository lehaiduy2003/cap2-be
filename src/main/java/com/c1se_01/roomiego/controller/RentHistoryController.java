package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.RentHistoryCreateRequest;
import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.service.RentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent-histories")
@RequiredArgsConstructor
public class RentHistoryController {
    private final RentHistoryService rentHistoryService;

    @PostMapping
    public ResponseEntity<RentHistoryResponse> createRentHistory(@RequestBody RentHistoryCreateRequest request) {
        RentHistoryResponse response = rentHistoryService.createRentHistory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RentHistoryResponse>> getRentHistoriesByUser(@PathVariable Long userId) {
        List<RentHistoryResponse> responses = rentHistoryService.getRentHistoriesByUser(userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/reviews/{rentHistoryId}")
    public ResponseEntity<Void> addReviewToRentHistory(@PathVariable Long rentHistoryId) {
        // Assuming the service has a method to add a review
        rentHistoryService.confirmReviewed(rentHistoryId);
        return ResponseEntity.noContent().build();
    }
}
