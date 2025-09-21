package com.c1se_01.roomiego.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ContractCreateRequest {
    private Long roomId;
    private Long tenantId;
    private Date startDate;
    private Date endDate;
    private BigDecimal pricePerMonth;
}

