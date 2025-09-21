package com.c1se_01.roomiego.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private Long roomId;
    private String reason;
}