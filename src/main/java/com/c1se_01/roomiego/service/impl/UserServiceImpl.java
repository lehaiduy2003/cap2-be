package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.MessageRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public List<User> findByFullName(String fullName) {
        // Lấy thông tin user hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        User user = (User) authentication.getPrincipal();
        String senderEmail = user.getEmail();

        // Tìm danh sách Message dựa trên senderName (email của user hiện tại)
        List<Message> messages = messageRepository.findBySenderName(senderEmail);

        // Lấy danh sách receiverName (email) từ messages
        List<String> receiverEmails = messages.stream()
                .map(Message::getReceiverName)
                .distinct() // Loại bỏ trùng lặp
                .collect(Collectors.toList());

        // Tìm tất cả user có email trong danh sách receiverEmails
        List<User> users = userRepository.findByEmailIn(receiverEmails);

        // Lọc user có fullName khớp với đầu vào (không phân biệt hoa thường)
        return users.stream()
                .filter(u -> u.getFullName().toLowerCase().contains(fullName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
