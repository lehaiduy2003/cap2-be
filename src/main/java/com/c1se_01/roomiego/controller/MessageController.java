package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.MessageService;
import com.c1se_01.roomiego.service.UserService;
import com.c1se_01.roomiego.enums.MessageType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
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
            @PathVariable String user2
    ) {
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
        String receiver = messageDto.getReceiverName();
        simpMessagingTemplate.convertAndSendToUser(receiver, "/private", messageDto);
        messageService.saveMessage(messageDto);
    }
}
