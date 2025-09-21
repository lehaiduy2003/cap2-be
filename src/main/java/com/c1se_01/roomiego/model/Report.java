package com.c1se_01.roomiego.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // bài đăng bị report

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // người gửi report

    @Column(name = "reason")
    private String reason; // lý do báo cáo

    @Column(name = "is_handled")
    private Boolean isHandled = false; // đã xử lý chưa

    @Column(name = "is_violation")
    private Boolean isViolation; // vi phạm hay không, null là chưa xử lý

    @Column(name = "admin_note")
    private String adminNote; // ghi chú xử lý

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "handled_at", updatable = false)
    private Date handledAt;
}

