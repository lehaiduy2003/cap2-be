package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.enums.MessageType;

import java.util.List;

public interface MessageService {
    Message sendMessage(SendMessageRequest request);

//    List<Message> getMessages(Long conversationId);

    void saveMessage(MessageDto messageDto);

    List<Message> findChatHistoryBetweenUsers(String user1, String user2);

    List<Message> findByType(MessageType type);
}
