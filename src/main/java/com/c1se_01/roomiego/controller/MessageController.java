package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ConversationSummaryDTO;
import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.MessageService;
import com.c1se_01.roomiego.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            return ResponseEntity.ok(messageService.sendMessage(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> findByUsername(@RequestParam String username) {
        List<User> users = userService.findByFullName(username);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/api/messages/history/public")
    public ResponseEntity<List<Message>> getPublicMessages() {
        return ResponseEntity.ok(messageService.findByType(com.c1se_01.roomiego.enums.MessageType.PUBLIC));
    }

    @GetMapping("/api/messages/history/{user1}/{user2}")
    public ResponseEntity<List<Message>> getChatHistory(
            @PathVariable String user1,
            @PathVariable String user2) {
        List<Message> messages = messageService.findChatHistoryBetweenUsers(user1, user2);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public MessageDto receiveMessage(MessageDto messageDto) throws InterruptedException {
        messageService.saveMessage(messageDto);
        Thread.sleep(1000);
        return messageDto;
    }

    @MessageMapping("/private-message")
    public void privateMessage(MessageDto messageDto) {
        log.info("Received private message from {} to {}", messageDto.getSenderName(), messageDto.getReceiverName());

        // Save the message first and get the saved message with conversationId
        Message savedMessage = messageService.saveMessageAndReturn(messageDto);

        log.info("Message saved with conversationId: {}", savedMessage.getConversationId());

        // Create response DTO with id, timestamp and conversationId from saved message
        MessageDto responseDto = new MessageDto();
        responseDto.setId(savedMessage.getId()); // Include the message ID
        responseDto.setTimestamp(savedMessage.getTimestamp()); // Include the timestamp
        responseDto.setSenderName(messageDto.getSenderName());
        responseDto.setSenderId(messageDto.getSenderId());
        responseDto.setReceiverName(messageDto.getReceiverName());
        responseDto.setReceiverId(messageDto.getReceiverId());
        responseDto.setMessage(messageDto.getMessage());
        responseDto.setMedia(messageDto.getMedia());
        responseDto.setMediaType(messageDto.getMediaType());
        responseDto.setStatus(messageDto.getStatus());
        responseDto.setType(messageDto.getType());
        responseDto.setConversationId(
                savedMessage.getConversationId() != null ? savedMessage.getConversationId().toString() : null);

        // Send to receiver via WebSocket
        String receiver = messageDto.getReceiverName();
        log.info("Sending message to receiver: {} at /user/{}/private", receiver, receiver);
        simpMessagingTemplate.convertAndSendToUser(receiver, "/private", responseDto);

        // Also send back to sender with the saved ID and timestamp
        String sender = messageDto.getSenderName();
        log.info("Sending message back to sender: {} at /user/{}/private", sender, sender);
        simpMessagingTemplate.convertAndSendToUser(sender, "/private", responseDto);

        log.info("Message broadcasting completed");
    }

    @GetMapping("/api/messages/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryDTO>> getConversations(@PathVariable Long userId) {
        List<ConversationSummaryDTO> conversations = messageService.getConversationsForUser(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long conversationId) {
        List<Message> messages = messageService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<List<Message>> getOrCreateConversation(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        List<Message> messages = messageService.getOrCreateConversationMessages(userId1, userId2);
        return ResponseEntity.ok(messages);
    }
}
