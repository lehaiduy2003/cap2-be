package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RENTER', 'OWNER')")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(user.getId());
        return ResponseEntity.ok(notifications);
    }
}
