package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.enums.MessageType;
import com.c1se_01.roomiego.enums.NotificationType;
import com.c1se_01.roomiego.enums.Status;
import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.model.Conversation;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.ConversationRepository;
import com.c1se_01.roomiego.repository.MessageRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.MessageService;
import com.c1se_01.roomiego.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    public Message sendMessage(SendMessageRequest request) {
        // Extract user IDs from conversation ID format (conv_user1_user2)
        String[] parts = request.getConversationId().split("_");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid conversation ID format");
        }
        
        Long user1Id = Long.parseLong(parts[1]);
        // Handle case when user2Id is undefined
        if ("undefined".equals(parts[2])) {
            throw new IllegalArgumentException("User2 ID is required for private messages");
        }
        
        Long user2Id = Long.parseLong(parts[2]);
        
        // Find or create conversation
        Conversation conversation = conversationRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .orElseGet(() -> {
                    User user1 = userRepository.findById(user1Id)
                            .orElseThrow(() -> new NotFoundException("User 1 not found"));
                    User user2 = userRepository.findById(user2Id)
                            .orElseThrow(() -> new NotFoundException("User 2 not found"));
                    
                    Conversation newConversation = new Conversation();
                    newConversation.setUser1(user1);
                    newConversation.setUser2(user2);
                    newConversation.setCreatedAt(new Date());
                    return conversationRepository.save(newConversation);
                });

        // Create and save message
        Message message = new Message();
        message.setSenderId(request.getSenderId());
        message.setMessage(request.getContent());
        message.setTimestamp(System.currentTimeMillis());
        message.setType(MessageType.PRIVATE);
        message.setStatus(Status.MESSAGE);
        message.setConversationId(conversation.getId());

        // Set sender and receiver names
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new NotFoundException("Sender not found"));
        message.setSenderName(sender.getFullName());
        
        User receiver = request.getSenderId().equals(user1Id) ? 
                conversation.getUser2() : conversation.getUser1();
        message.setReceiverName(receiver.getFullName());
        message.setReceiverId(receiver.getId());

        Message savedMessage = messageRepository.save(message);

        // Send WebSocket notification
        messagingTemplate.convertAndSend("/topic/messages/" + request.getConversationId(), savedMessage);

        return savedMessage;
    }

//    @Override
//    public List<Message> getMessages(Long conversationId) {
//        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
//        return messageRepository.findByConversationOrderBySentAt(conversation);
//    }

    @Override
    public void saveMessage(MessageDto messageDto) {
        // Validate message type and receiver
        if (messageDto.getType() == MessageType.PRIVATE && messageDto.getReceiverName() == null) {
            throw new IllegalArgumentException("Receiver name cannot be null for private messages");
        }

        // Save to the database
        messageRepository.save(new Message(
                messageDto.getSenderName(),
                messageDto.getReceiverName(),  // Can be null for public messages
                messageDto.getMessage(),
                messageDto.getMedia(),
                messageDto.getMediaType(),
                messageDto.getStatus(),
                System.currentTimeMillis(),  // Current timestamp
                messageDto.getType()
        ));
    }

    @Override
    public List<Message> findChatHistoryBetweenUsers(String user1, String user2) {
        return messageRepository.findChatHistoryBetweenUsers(user1, user2);
    }

    @Override
    public List<Message> findByType(MessageType type) {
        return messageRepository.findByType(type);
    }
}
