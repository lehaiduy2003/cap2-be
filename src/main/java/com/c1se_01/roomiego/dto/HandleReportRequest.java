package com.c1se_01.roomiego.dto;

import lombok.Data;

@Data
public class HandleReportRequest {
    private Boolean isViolation;
    private String adminNote;
}
