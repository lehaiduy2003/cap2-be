package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.StartChatRequest;
import com.c1se_01.roomiego.model.Conversation;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/conversations")
public class ConversationController {
    private final ConversationService conversationService;

//    @PostMapping("/start")
//    public ResponseEntity<Conversation> startConversation(@RequestBody StartChatRequest request) {
//        Conversation conversation = conversationService.startConversation(request.getUser1Id(), request.getUser2Id());
//        return ResponseEntity.ok(conversation);
//    }
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<User>> getUsersChattedWith(@PathVariable Long userId) {
//        List<User> users = conversationService.getUsersChattedWith(userId);
//        return ResponseEntity.ok(users);
//    }
}
