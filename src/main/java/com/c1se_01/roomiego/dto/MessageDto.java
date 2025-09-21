package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.enums.MessageType;
import com.c1se_01.roomiego.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class MessageDto {
    private String senderName;

    private String receiverName;

    private String message;

    private String media;

    private Status status;

    private String mediaType;

    private MessageType type;
}
