package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ConversationSummaryDTO;
import com.c1se_01.roomiego.dto.MessageDto;
import com.c1se_01.roomiego.dto.SendMessageRequest;
import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.MessageService;
import com.c1se_01.roomiego.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {

  private MockMvc mockMvc;

  @Mock
  private MessageService messageService;

  @Mock
  private UserService userService;

  @Mock
  private SimpMessagingTemplate simpMessagingTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  private SendMessageRequest sendMessageRequest;
  private Message message;
  private MessageDto messageDto;
  private User user;

  @BeforeEach
  void setUp() {
    sendMessageRequest = new SendMessageRequest();
    sendMessageRequest.setConversationId("conv_1");
    sendMessageRequest.setSenderId(1L);
    sendMessageRequest.setContent("Hello");

    message = new Message();
    message.setId(1L);
    message.setMessage("Hello");

    messageDto = new MessageDto();
    messageDto.setSenderName("sender");
    messageDto.setReceiverName("receiver");
    messageDto.setMessage("Hello");

    user = new User();
    user.setId(1L);
    user.setFullName("Test User");

    MessageController messageController = new MessageController(messageService, simpMessagingTemplate, userService);
    mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
  }

  @Test
  @WithMockUser
  void sendMessage_HappyCase() throws Exception {
    when(messageService.sendMessage(any(SendMessageRequest.class))).thenReturn(message);

    mockMvc.perform(post("/messages/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sendMessageRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.message").value("Hello"));
  }

  @Test
  @WithMockUser
  void sendMessage_InvalidRequest() throws Exception {
    // Test with null content
    sendMessageRequest.setContent(null);

    when(messageService.sendMessage(any(SendMessageRequest.class))).thenReturn(message);

    mockMvc.perform(post("/messages/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sendMessageRequest)))
        .andExpect(status().isOk()); // Since no validation, it still proceeds
  }

  @Test
  @WithMockUser
  void sendMessage_ServiceException() throws Exception {
    when(messageService.sendMessage(any(SendMessageRequest.class)))
        .thenThrow(new RuntimeException("Service error"));

    mockMvc.perform(post("/messages/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(sendMessageRequest)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser
  void logout_HappyCase() throws Exception {
    MockHttpSession session = new MockHttpSession();

    mockMvc.perform(get("/messages/logout").session(session))
        .andExpect(status().isOk())
        .andExpect(content().string("Logged out successfully"));
  }

  @Test
  @WithMockUser
  void findByUsername_NoUsersFound() throws Exception {
    when(userService.findByFullName("nonexistent")).thenReturn(null);

    mockMvc.perform(get("/messages/search").param("username", "nonexistent"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser
  void findByUsername_EmptyList() throws Exception {
    when(userService.findByFullName("empty")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/messages/search").param("username", "empty"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser
  void getPublicMessages_HappyCase() throws Exception {
    List<Message> messages = Arrays.asList(message);
    when(messageService.findByType(any())).thenReturn(messages);

    mockMvc.perform(get("/messages/api/messages/history/public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].message").value("Hello"));
  }

  @Test
  @WithMockUser
  void getPublicMessages_EmptyList() throws Exception {
    when(messageService.findByType(any())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/messages/api/messages/history/public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser
  void getChatHistory_HappyCase() throws Exception {
    List<Message> messages = Arrays.asList(message);
    when(messageService.findChatHistoryBetweenUsers("user1", "user2")).thenReturn(messages);

    mockMvc.perform(get("/messages/api/messages/history/user1/user2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].message").value("Hello"));
  }

  @Test
  @WithMockUser
  void getChatHistory_EmptyHistory() throws Exception {
    when(messageService.findChatHistoryBetweenUsers("user1", "user2")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/messages/api/messages/history/user1/user2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser
  void getConversations_HappyCase() throws Exception {
    List<ConversationSummaryDTO> conversations = Arrays.asList(new ConversationSummaryDTO());
    when(messageService.getConversationsForUser(1L)).thenReturn(conversations);

    mockMvc.perform(get("/messages/api/messages/conversations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  @WithMockUser
  void getConversationMessages_HappyCase() throws Exception {
    List<Message> messages = Arrays.asList(message);
    when(messageService.getMessagesByConversationId(1L)).thenReturn(messages);

    mockMvc.perform(get("/messages/conversation/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L));
  }

  @Test
  @WithMockUser
  void getOrCreateConversation_HappyCase() throws Exception {
    List<Message> messages = Arrays.asList(message);
    when(messageService.getOrCreateConversationMessages(1L, 2L)).thenReturn(messages);

    mockMvc.perform(get("/messages/conversation/1/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L));
  }

}