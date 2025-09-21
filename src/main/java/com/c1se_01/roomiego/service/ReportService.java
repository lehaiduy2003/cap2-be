package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.common.ResponseData;
import com.c1se_01.roomiego.dto.HandleReportRequest;
import com.c1se_01.roomiego.dto.ReportRequest;
import com.c1se_01.roomiego.dto.ReportResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void reportRoom(ReportRequest request);

    ResponseData getReports(Boolean isHandled, Pageable pageable);

    void handleReport(Long reportId, HandleReportRequest request);

    ReportResponse getReportById(Long id);

    void deleteReport(Long id);
}
