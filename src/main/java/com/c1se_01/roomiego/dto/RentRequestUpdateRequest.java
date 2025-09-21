package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.RentRequestStatus;
import lombok.Data;

@Data
public class RentRequestUpdateRequest {
    private RentRequestStatus status; // APPROVED hoặc REJECTED
}
