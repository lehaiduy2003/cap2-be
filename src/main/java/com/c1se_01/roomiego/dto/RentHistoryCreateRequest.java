package com.c1se_01.roomiego.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RentHistoryCreateRequest {
    private Long userId;
    private Long rentRequestId;
    private LocalDateTime rentDate;
    private LocalDateTime returnDate;
}