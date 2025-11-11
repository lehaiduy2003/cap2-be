package com.c1se_01.roomiego.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDTO {
    private Long conversationId;
    private Long partnerId;
    private String partnerEmail;
    private String partnerName;
    private String partnerPhone;
    private String partnerRole;
    private String lastMessage;
    private Long lastTimestamp;
}

