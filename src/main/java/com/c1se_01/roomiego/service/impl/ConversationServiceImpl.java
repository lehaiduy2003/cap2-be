package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.model.Conversation;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.ConversationRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

//    @Override
//    public Conversation startConversation(Long user1Id, Long user2Id) {
//        User user1 = userRepository.findById(user1Id)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//        User user2 = userRepository.findById(user2Id)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//
//        // Kiểm tra nếu cuộc trò chuyện đã tồn tại
//        Optional<Conversation> existingConversation = conversationRepository.findExistingConversation(user1Id, user2Id);
//        if (existingConversation.isPresent()) {
//            return existingConversation.get();
//        }
//
//        // Tạo cuộc trò chuyện mới
//        Conversation newConversation = new Conversation();
//        newConversation.setUser1(user1);
//        newConversation.setUser2(user2);
//
//        return conversationRepository.save(newConversation);
//    }
//
//    @Override
//    public List<User> getUsersChattedWith(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User không tồn tại"));
//        List<Conversation> conversations = conversationRepository.findAllByUser(user);
//
//        // Lọc ra danh sách user đã nhắn tin, bỏ qua chính user hiện tại
//        return conversations.stream()
//                .map(c -> c.getUser1().equals(user) ? c.getUser2() : c.getUser1())
//                .distinct()
//                .collect(Collectors.toList());
//    }
}
