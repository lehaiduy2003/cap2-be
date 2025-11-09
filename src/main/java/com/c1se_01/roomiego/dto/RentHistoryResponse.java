package com.c1se_01.roomiego.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RentHistoryResponse {
    private Long id;
    private Long userId;
    private Long rentRequestId;
    private Long roomId;
    private String roomTitle;
    private String address;
    private String description;
    private Double longitude;
    private Double latitude;
    private LocalDateTime rentDate;
    private LocalDateTime returnDate;
    private Boolean reviewed;
}