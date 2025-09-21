package com.c1se_01.roomiego.dto;

import lombok.Data;

@Data
public class ViewRespondDTO {
    private Long requestId;
    private boolean accept;
    private String adminNote;
}
