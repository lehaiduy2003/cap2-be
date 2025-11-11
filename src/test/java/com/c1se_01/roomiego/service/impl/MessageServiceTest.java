package com.c1se_01.roomiego.service.impl;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ConversationRepository conversationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private MessageServiceImpl messageService;

  private SendMessageRequest sendMessageRequest;
  private MessageDto messageDto;
  private User user1;
  private User user2;
  private Conversation conversation;
  private Message message;

  @BeforeEach
  void setUp() {
    user1 = new User();
    user1.setId(1L);
    user1.setEmail("user1@example.com");

    user2 = new User();
    user2.setId(2L);
    user2.setEmail("user2@example.com");

    conversation = new Conversation();
    conversation.setId(1L);
    conversation.setUser1(user1);
    conversation.setUser2(user2);
    conversation.setCreatedAt(new Date());

    message = new Message();
    message.setId(1L);
    message.setSenderId(1L);
    message.setSenderName("user1@example.com");
    message.setReceiverId(2L);
    message.setReceiverName("user2@example.com");
    message.setMessage("Test message");
    message.setTimestamp(System.currentTimeMillis());
    message.setType(MessageType.PRIVATE);
    message.setStatus(Status.MESSAGE);
    message.setConversationId(1L);

    sendMessageRequest = new SendMessageRequest();
    sendMessageRequest.setConversationId("conv_1_2");
    sendMessageRequest.setSenderId(1L);
    sendMessageRequest.setContent("Test content");

    messageDto = new MessageDto();
    messageDto.setSenderId(1L);
    messageDto.setSenderName("user1@example.com");
    messageDto.setReceiverId(2L);
    messageDto.setReceiverName("user2@example.com");
    messageDto.setMessage("Test message");
    messageDto.setType(MessageType.PRIVATE);
    messageDto.setStatus(Status.MESSAGE);
  }

  // Tests for sendMessage
  @Test
  void sendMessage_ConversationExists_Success() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(conversation));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Message result = messageService.sendMessage(sendMessageRequest);

    assertNotNull(result);
    assertEquals("Test content", result.getMessage());
    verify(conversationRepository, times(1)).findByUser1IdAndUser2Id(1L, 2L);
    verify(messageRepository, times(1)).save(any(Message.class));
    verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Message.class));
  }

  @Test
  void sendMessage_ConversationNotExists_CreatesNew() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
    when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Message result = messageService.sendMessage(sendMessageRequest);

    assertNotNull(result);
    verify(conversationRepository, times(1)).save(any(Conversation.class));
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void sendMessage_InvalidConversationIdFormat_ThrowsException() {
    sendMessageRequest.setConversationId("invalid");

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(sendMessageRequest));
  }

  @Test
  void sendMessage_User2Undefined_ThrowsException() {
    sendMessageRequest.setConversationId("conv_1_undefined");

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(sendMessageRequest));
  }

  @Test
  void sendMessage_User1NotFound_ThrowsException() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> messageService.sendMessage(sendMessageRequest));
  }

  @Test
  void sendMessage_User2NotFound_ThrowsException() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(userRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> messageService.sendMessage(sendMessageRequest));
  }

  @Test
  void sendMessage_SenderNotFound_ThrowsException() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(conversation));
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> messageService.sendMessage(sendMessageRequest));
  }

  // Tests for saveMessage
  @Test
  void saveMessage_PrivateMessageWithReceiver_Success() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(conversation));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertDoesNotThrow(() -> messageService.saveMessage(messageDto));
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void saveMessage_PrivateMessageWithoutReceiver_ThrowsException() {
    messageDto.setReceiverName(null);

    assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(messageDto));
  }

  @Test
  void saveMessage_ResolveSenderIdFromName() {
    messageDto.setSenderId(null);
    when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(conversation));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    messageService.saveMessage(messageDto);

    verify(userRepository, times(1)).findByEmail("user1@example.com");
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void saveMessage_ResolveReceiverIdFromName() {
    messageDto.setReceiverId(null);
    when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(user2));
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(conversation));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    messageService.saveMessage(messageDto);

    verify(userRepository, times(1)).findByEmail("user2@example.com");
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void saveMessage_ConversationNotExists_CreatesNew() {
    when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
    when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    messageService.saveMessage(messageDto);

    verify(conversationRepository, times(1)).save(any(Conversation.class));
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void saveMessage_WithConversationIdString_ParsesCorrectly() {
    messageDto.setSenderId(null);
    messageDto.setReceiverId(null);
    messageDto.setConversationId("123");

    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    messageService.saveMessage(messageDto);

    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  void saveMessage_WithInvalidConversationIdString_Ignores() {
    messageDto.setSenderId(null);
    messageDto.setReceiverId(null);
    messageDto.setConversationId("invalid");

    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    messageService.saveMessage(messageDto);

    verify(messageRepository, times(1)).save(any(Message.class));
  }

  // Tests for findChatHistoryBetweenUsers
  @Test
  void findChatHistoryBetweenUsers_BothUsersResolved_ReturnsMessages() {
    List<Message> messages = List.of(message);
    when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
    when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(user2));
    when(messageRepository.findChatHistoryBetweenUsers(1L, 2L)).thenReturn(messages);

    List<Message> result = messageService.findChatHistoryBetweenUsers("user1@example.com", "user2@example.com");

    assertEquals(1, result.size());
    verify(messageRepository, times(1)).findChatHistoryBetweenUsers(1L, 2L);
  }

  @Test
  void findChatHistoryBetweenUsers_User1NotResolved_ReturnsEmpty() {
    when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.empty());

    List<Message> result = messageService.findChatHistoryBetweenUsers("user1@example.com", "user2@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  void findChatHistoryBetweenUsers_User2NotResolved_ReturnsEmpty() {
    when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
    when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.empty());

    List<Message> result = messageService.findChatHistoryBetweenUsers("user1@example.com", "user2@example.com");

    assertTrue(result.isEmpty());
  }

  // Tests for findByType
  @Test
  void findByType_ReturnsMessages() {
    List<Message> messages = List.of(message);
    when(messageRepository.findByType(MessageType.PRIVATE)).thenReturn(messages);

    List<Message> result = messageService.findByType(MessageType.PRIVATE);

    assertEquals(1, result.size());
    verify(messageRepository, times(1)).findByType(MessageType.PRIVATE);
  }
}