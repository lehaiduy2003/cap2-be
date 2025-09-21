package com.c1se_01.roomiego.model;

import com.c1se_01.roomiego.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người nhận

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}
