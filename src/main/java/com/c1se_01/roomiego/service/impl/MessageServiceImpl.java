package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.ConversationSummaryDTO;
import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.enums.MessageType;
import com.c1se_01.roomiego.enums.Status;
import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.model.Conversation;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.ConversationRepository;
import com.c1se_01.roomiego.repository.MessageRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
        message.setSenderName(sender.getEmail());

        User receiver = request.getSenderId().equals(user1Id) ? conversation.getUser2() : conversation.getUser1();
        message.setReceiverName(receiver.getEmail());
        message.setReceiverId(receiver.getId());

        Message savedMessage = messageRepository.save(message);

        // Send WebSocket notification
        messagingTemplate.convertAndSend("/topic/messages/" + request.getConversationId(), savedMessage);

        return savedMessage;
    }

    // @Override
    // public List<Message> getMessages(Long conversationId) {
    // Conversation conversation =
    // conversationRepository.findById(conversationId).orElseThrow();
    // return messageRepository.findByConversationOrderBySentAt(conversation);
    // }

    @Override
    public void saveMessage(MessageDto messageDto) {
        // Validate message type and receiver
        if (messageDto.getType() == MessageType.PRIVATE && messageDto.getReceiverName() == null) {
            throw new IllegalArgumentException("Receiver name cannot be null for private messages");
        }

        // Save to the database
        Message message = new Message();
        message.setSenderId(messageDto.getSenderId());
        message.setSenderName(messageDto.getSenderName());
        message.setReceiverId(messageDto.getReceiverId());
        message.setReceiverName(messageDto.getReceiverName());
        message.setMessage(messageDto.getMessage());
        message.setMedia(messageDto.getMedia());
        message.setMediaType(messageDto.getMediaType());
        message.setStatus(messageDto.getStatus());
        message.setTimestamp(System.currentTimeMillis());
        message.setType(messageDto.getType());

        Long resolvedSenderId = messageDto.getSenderId();
        Long resolvedReceiverId = messageDto.getReceiverId();
        if (resolvedSenderId == null && messageDto.getSenderName() != null) {
            resolvedSenderId = resolveUserId(messageDto.getSenderName());
            message.setSenderId(resolvedSenderId);
        }
        if (resolvedReceiverId == null && messageDto.getReceiverName() != null) {
            resolvedReceiverId = resolveUserId(messageDto.getReceiverName());
            message.setReceiverId(resolvedReceiverId);
        }
        log.info("Saving message from {} ({}) to {} ({})", messageDto.getSenderName(), resolvedSenderId,
                messageDto.getReceiverName(), resolvedReceiverId);
        String conversationId = messageDto.getConversationId();
        if (resolvedSenderId != null && resolvedReceiverId != null) {
            final Long senderIdFinal = resolvedSenderId;
            final Long receiverIdFinal = resolvedReceiverId;
            Conversation conversation = conversationRepository
                    .findByUser1IdAndUser2Id(senderIdFinal, receiverIdFinal)
                    .orElseGet(() -> {
                        User sender = userRepository.findById(senderIdFinal)
                                .orElseThrow(() -> new NotFoundException("Sender not found"));
                        User receiver = userRepository.findById(receiverIdFinal)
                                .orElseThrow(() -> new NotFoundException("Receiver not found"));
                        Conversation newConversation = new Conversation();
                        newConversation.setUser1(sender);
                        newConversation.setUser2(receiver);
                        newConversation.setCreatedAt(new Date());
                        return conversationRepository.save(newConversation);
                    });
            message.setConversationId(conversation.getId());
        } else if (conversationId != null) {
            try {
                Long numericConversationId = Long.parseLong(conversationId);
                message.setConversationId(numericConversationId);
            } catch (NumberFormatException ignored) {
            }
        }

        messageRepository.save(message);
    }

    @Override
    public Message saveMessageAndReturn(MessageDto messageDto) {
        // Validate message type and receiver
        if (messageDto.getType() == MessageType.PRIVATE && messageDto.getReceiverName() == null) {
            throw new IllegalArgumentException("Receiver name cannot be null for private messages");
        }

        // Save to the database
        Message message = new Message();
        message.setSenderId(messageDto.getSenderId());
        message.setSenderName(messageDto.getSenderName());
        message.setReceiverId(messageDto.getReceiverId());
        message.setReceiverName(messageDto.getReceiverName());
        message.setMessage(messageDto.getMessage());
        message.setMedia(messageDto.getMedia());
        message.setMediaType(messageDto.getMediaType());
        message.setStatus(messageDto.getStatus());
        message.setTimestamp(System.currentTimeMillis());
        message.setType(messageDto.getType());

        Long resolvedSenderId = messageDto.getSenderId();
        Long resolvedReceiverId = messageDto.getReceiverId();
        if (resolvedSenderId == null && messageDto.getSenderName() != null) {
            resolvedSenderId = resolveUserId(messageDto.getSenderName());
            message.setSenderId(resolvedSenderId);
        }
        if (resolvedReceiverId == null && messageDto.getReceiverName() != null) {
            resolvedReceiverId = resolveUserId(messageDto.getReceiverName());
            message.setReceiverId(resolvedReceiverId);
        }

        log.info("Saving message from {} ({}) to {} ({})", messageDto.getSenderName(), resolvedSenderId,
                messageDto.getReceiverName(), resolvedReceiverId);

        String conversationId = messageDto.getConversationId();
        if (resolvedSenderId != null && resolvedReceiverId != null) {
            final Long senderIdFinal = resolvedSenderId;
            final Long receiverIdFinal = resolvedReceiverId;
            Conversation conversation = conversationRepository
                    .findByUser1IdAndUser2Id(senderIdFinal, receiverIdFinal)
                    .orElseGet(() -> {
                        User sender = userRepository.findById(senderIdFinal)
                                .orElseThrow(() -> new NotFoundException("Sender not found"));
                        User receiver = userRepository.findById(receiverIdFinal)
                                .orElseThrow(() -> new NotFoundException("Receiver not found"));
                        Conversation newConversation = new Conversation();
                        newConversation.setUser1(sender);
                        newConversation.setUser2(receiver);
                        newConversation.setCreatedAt(new Date());
                        log.info("Creating new conversation between {} and {}", senderIdFinal, receiverIdFinal);
                        return conversationRepository.save(newConversation);
                    });
            message.setConversationId(conversation.getId());
            log.info("Message assigned to conversation ID: {}", conversation.getId());
        } else if (conversationId != null) {
            try {
                Long numericConversationId = Long.parseLong(conversationId);
                message.setConversationId(numericConversationId);
            } catch (NumberFormatException ignored) {
            }
        }

        return messageRepository.save(message);
    }

    @Override
    public List<Message> findChatHistoryBetweenUsers(String user1, String user2) {
        Long user1Id = resolveUserId(user1);
        Long user2Id = resolveUserId(user2);
        if (user1Id == null || user2Id == null) {
            return List.of();
        }
        return messageRepository.findChatHistoryBetweenUsers(user1Id, user2Id);
    }

    @Override
    public List<Message> findByType(MessageType type) {
        return messageRepository.findByType(type);
    }

    @Override
    public List<ConversationSummaryDTO> getConversationsForUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);
        return conversations.stream()
                .map(conversation -> {
                    User partner = conversation.getUser1().getId().equals(userId)
                            ? conversation.getUser2()
                            : conversation.getUser1();

                    Message lastMessage = messageRepository
                            .findTopByConversationIdOrderByTimestampDesc(conversation.getId());

                    ConversationSummaryDTO dto = new ConversationSummaryDTO();
                    dto.setConversationId(conversation.getId());
                    dto.setPartnerId(partner.getId());
                    dto.setPartnerEmail(partner.getEmail());
                    dto.setPartnerName(partner.getFullName());
                    dto.setPartnerPhone(partner.getPhone());
                    dto.setPartnerRole(partner.getRole() != null ? partner.getRole().name() : null);
                    dto.setLastMessage(lastMessage != null ? lastMessage.getMessage() : null);
                    Long lastTimestamp = lastMessage != null
                            ? lastMessage.getTimestamp()
                            : conversation.getCreatedAt() != null
                                    ? conversation.getCreatedAt().getTime()
                                    : new Date().getTime();
                    dto.setLastTimestamp(lastTimestamp);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> getMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Override
    public List<Message> getOrCreateConversationMessages(Long userId1, Long userId2) {
        // Find or create conversation
        Conversation conversation = conversationRepository.findByUser1IdAndUser2Id(userId1, userId2)
                .orElseGet(() -> {
                    User user1 = userRepository.findById(userId1)
                            .orElseThrow(() -> new NotFoundException("User 1 not found"));
                    User user2 = userRepository.findById(userId2)
                            .orElseThrow(() -> new NotFoundException("User 2 not found"));

                    Conversation newConversation = new Conversation();
                    newConversation.setUser1(user1);
                    newConversation.setUser2(user2);
                    newConversation.setCreatedAt(new Date());
                    return conversationRepository.save(newConversation);
                });

        // Return messages for this conversation
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversation.getId());
    }

    private Long resolveUserId(String identifier) {
        if (identifier == null) {
            return null;
        }
        String normalized = identifier.trim().toLowerCase();
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return userRepository.findByEmail(normalized)
                    .map(User::getId)
                    .orElse(null);
        }
    }
}
