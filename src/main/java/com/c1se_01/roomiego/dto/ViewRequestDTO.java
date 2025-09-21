package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.ViewRequestStatus;
import lombok.Data;

import java.util.Date;

@Data
public class ViewRequestDTO {
    private Long id;
    private Long roomId;
    private Long renterId;
    private Long ownerId;
    private String message;
    private ViewRequestStatus status;
    private String adminNote;
    private Date createdAt;
}
