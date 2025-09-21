package com.c1se_01.roomiego.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long roomId;
    private String roomTitle;
    private String roomAddress; // ✅ Thêm dòng này
    private String reporterName;
    private String reason;
    private Boolean isHandled;
    private Boolean isViolation;
    private String adminNote;
    private Date createdAt;
}
