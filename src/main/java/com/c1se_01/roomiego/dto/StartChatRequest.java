package com.c1se_01.roomiego.dto;

import lombok.Data;

@Data
public class StartChatRequest {
    private Long user1Id;
    private Long user2Id;
}
