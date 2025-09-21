package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ContractResponse {
    private Long id;
    private Long roomId;
    private Long tenantId;
    private Long ownerId;
    private Date startDate;
    private Date endDate;
    private BigDecimal pricePerMonth;
    private PaymentStatus paymentStatus;
    private String contractHash;
}
