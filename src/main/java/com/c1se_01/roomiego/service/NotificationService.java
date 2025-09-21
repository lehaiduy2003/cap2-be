package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    void sendNotificationToUser(Long userId, String message);

    void saveNotification(NotificationDto notificationDto);

    List<NotificationDto> getNotificationsForUser(Long userId);

}
