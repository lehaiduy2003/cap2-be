package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.enums.NotificationType;
import com.c1se_01.roomiego.model.Notification;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.NotificationRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  private NotificationDto notificationDto;
  private User user;
  private Notification notification;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("user@example.com");

    notification = new Notification();
    notification.setId(1L);
    notification.setUser(user);
    notification.setMessage("Test message");
    notification.setType(NotificationType.RENT_REQUEST_CREATED);
    notification.setCreatedAt(new Date());

    notificationDto = new NotificationDto();
    notificationDto.setUserId(1L);
    notificationDto.setMessage("Test message");
    notificationDto.setType(NotificationType.RENT_REQUEST_CREATED);
  }

  // Tests for sendNotificationToUser
  @Test
  void sendNotificationToUser_Success() {
    notificationService.sendNotificationToUser(1L, "Test message");

    verify(messagingTemplate, times(1)).convertAndSend("/topic/notifications/1", "Test message");
  }

  // Tests for saveNotification
  @Test
  void saveNotification_UserExists_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertDoesNotThrow(() -> notificationService.saveNotification(notificationDto));
    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  void saveNotification_UserNotFound_SavesWithNullUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertDoesNotThrow(() -> notificationService.saveNotification(notificationDto));
    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  // Tests for getNotificationsForUser
  @Test
  void getNotificationsForUser_ReturnsNotifications() {
    List<Notification> notifications = List.of(notification);
    when(notificationRepository.findByUserId(1L)).thenReturn(notifications);

    List<NotificationDto> result = notificationService.getNotificationsForUser(1L);

    assertEquals(1, result.size());
    assertEquals("Test message", result.get(0).getMessage());
    verify(notificationRepository, times(1)).findByUserId(1L);
  }

  @Test
  void getNotificationsForUser_EmptyList() {
    when(notificationRepository.findByUserId(1L)).thenReturn(List.of());

    List<NotificationDto> result = notificationService.getNotificationsForUser(1L);

    assertTrue(result.isEmpty());
    verify(notificationRepository, times(1)).findByUserId(1L);
  }
}