package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.model.Notification;
import com.c1se_01.roomiego.repository.NotificationRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    public void sendNotificationToUser(Long userId, String message) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
    }

    @Override
    public void saveNotification(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setUser(userRepository.findById(notificationDto.getUserId()).orElse(null));
        notification.setMessage(notificationDto.getMessage());
        notification.setType(notificationDto.getType());
        notification.setCreatedAt(new Date());
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDto> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(notification -> new NotificationDto(
                        notification.getUser().getId(),
                        notification.getMessage(),
                        notification.getType()))
                .collect(Collectors.toList());
    }
}
