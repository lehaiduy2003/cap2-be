package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.RentRequestStatus;
import lombok.Data;

import java.util.Date;

@Data
public class RentRequestResponse {
    private Long id;
    private Long tenantId;
    private Long roomId;
    private RentRequestStatus status;
    private Date createdAt;
    private Boolean tenantFinalize;
    private Boolean ownerFinalize;
    private String message;
    private String adminNote;
}
