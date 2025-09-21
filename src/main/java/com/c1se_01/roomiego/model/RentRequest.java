package com.c1se_01.roomiego.model;

import com.c1se_01.roomiego.enums.RentRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "rent_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentRequestStatus status = RentRequestStatus.PENDING;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt = new Date();

    @Column(name = "tenant_finalize")
    private Boolean tenantFinalize = false;

    @Column(name = "owner_finalize")
    private Boolean ownerFinalize = false;
}
