package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.dto.ViewRespondDTO;
import com.c1se_01.roomiego.service.ViewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/view-requests")
@RequiredArgsConstructor
public class ViewRequestController {

    private final ViewRequestService viewRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    public ResponseEntity<ViewRequestDTO> createRequest(@RequestBody ViewRequestCreateDTO dto) {
        ViewRequestDTO createdRequest = viewRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')") // chỉ Admin và Owner mới vào được
    public ResponseEntity<List<ViewRequestDTO>> getRequestsByOwner() {
        List<ViewRequestDTO> requests = viewRequestService.getRequestsByOwner();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/respond")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')") // chỉ Admin và Owner mới vào được
    public ResponseEntity<ViewRequestDTO> respondToRequest(@RequestBody ViewRespondDTO viewRespondDTO) {
        ViewRequestDTO response = viewRequestService.respondToRequest(viewRespondDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel-rental")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ViewRequestDTO> cancelRental(@RequestBody ViewRespondDTO viewRespondDTO) {
        ViewRequestDTO response = viewRequestService.cancelRental(viewRespondDTO);
        return ResponseEntity.ok(response);
    }
}


