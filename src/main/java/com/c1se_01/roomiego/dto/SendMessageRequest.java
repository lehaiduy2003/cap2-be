package com.c1se_01.roomiego.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String conversationId;
    private Long senderId;
    private String content;
    
    // Helper method to extract numeric ID if needed
    public Long getNumericConversationId() {
        if (conversationId == null || !conversationId.startsWith("conv_")) {
            return null;
        }
        try {
            // Extract the last number after the last underscore
            String[] parts = conversationId.split("_");
            if (parts.length >= 2) {
                return Long.parseLong(parts[parts.length - 1]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
