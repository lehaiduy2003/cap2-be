package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDto {
    private Long userId;
    private String message;
    private NotificationType type;
}