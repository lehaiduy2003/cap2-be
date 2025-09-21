package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.common.ResponseData;
import com.c1se_01.roomiego.dto.HandleReportRequest;
import com.c1se_01.roomiego.dto.ReportRequest;
import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> reportRoom(@RequestBody ReportRequest request) {
        reportService.reportRoom(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')") // chỉ Admin mới vào được
    public ResponseEntity<? extends ResponseData> getReports(
            @RequestParam(required = false) Boolean isHandled,
            @RequestParam(value = "page", required = false) final Optional<Integer> page,
            @RequestParam(value = "limit", required = false) final Optional<Integer> size) {
        final Pageable pageable =
                PageRequest.of(page.orElse(1) - 1, size.orElse(25));
        return ResponseEntity.ok(reportService.getReports(isHandled, pageable));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasRole('ADMIN')") // chỉ Admin mới vào được
    public ResponseEntity<Void> handleReport(
            @PathVariable Long id,
            @RequestBody HandleReportRequest request) {
        reportService.handleReport(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // chỉ Admin mới vào được
    public ResponseEntity<ReportResponse> getReportDetail(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

}
