package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.MessageRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private UserServiceImpl userService;

  private User currentUser;
  private User receiver1;
  private User receiver2;
  private Message message1;
  private Message message2;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("current@example.com");
    currentUser.setFullName("Current User");

    receiver1 = new User();
    receiver1.setId(2L);
    receiver1.setEmail("receiver1@example.com");
    receiver1.setFullName("John Doe");

    receiver2 = new User();
    receiver2.setId(3L);
    receiver2.setEmail("receiver2@example.com");
    receiver2.setFullName("Jane Smith");

    message1 = new Message();
    message1.setId(1L);
    message1.setSenderId(1L);
    message1.setReceiverName("receiver1@example.com");

    message2 = new Message();
    message2.setId(2L);
    message2.setSenderId(1L);
    message2.setReceiverName("receiver2@example.com");
  }

  @Test
  void findByFullName_SuccessfulFindWithMatches() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1, message2));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com")))
          .thenReturn(Arrays.asList(receiver1, receiver2));

      List<User> result = userService.findByFullName("John");

      assertEquals(1, result.size());
      assertEquals("John Doe", result.get(0).getFullName());
      verify(messageRepository, times(1)).findBySenderId(1L);
      verify(userRepository, times(1)).findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com"));
    }
  }

  @Test
  void findByFullName_SuccessfulFindWithMultipleMatches() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1, message2));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com")))
          .thenReturn(Arrays.asList(receiver1, receiver2));

      List<User> result = userService.findByFullName("Doe");

      assertEquals(1, result.size());
      assertEquals("John Doe", result.get(0).getFullName());
    }
  }

  @Test
  void findByFullName_SuccessfulFindCaseInsensitive() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1, message2));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com")))
          .thenReturn(Arrays.asList(receiver1, receiver2));

      List<User> result = userService.findByFullName("jane");

      assertEquals(1, result.size());
      assertEquals("Jane Smith", result.get(0).getFullName());
    }
  }

  @Test
  void findByFullName_SuccessfulFindEmptyFullName() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1, message2));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com")))
          .thenReturn(Arrays.asList(receiver1, receiver2));

      List<User> result = userService.findByFullName("");

      assertEquals(2, result.size());
    }
  }

  @Test
  void findByFullName_NoMessages() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Collections.emptyList());
      when(userRepository.findByEmailIn(Collections.emptyList())).thenReturn(Collections.emptyList());

      List<User> result = userService.findByFullName("John");

      assertTrue(result.isEmpty());
      verify(messageRepository, times(1)).findBySenderId(1L);
      verify(userRepository, times(1)).findByEmailIn(Collections.emptyList());
    }
  }

  @Test
  void findByFullName_MessagesButNoUsersFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com")))
          .thenReturn(Collections.emptyList());

      List<User> result = userService.findByFullName("John");

      assertTrue(result.isEmpty());
    }
  }

  @Test
  void findByFullName_UsersFoundButNoMatches() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(currentUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(messageRepository.findBySenderId(1L)).thenReturn(Arrays.asList(message1, message2));
      when(userRepository.findByEmailIn(Arrays.asList("receiver1@example.com", "receiver2@example.com")))
          .thenReturn(Arrays.asList(receiver1, receiver2));

      List<User> result = userService.findByFullName("NonExistent");

      assertTrue(result.isEmpty());
    }
  }

  @Test
  void findByFullName_NoAuthenticatedUser_AuthenticationNull() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(null);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findByFullName("John"));
      assertEquals("No authenticated user found", exception.getMessage());
    }
  }

  @Test
  void findByFullName_NoAuthenticatedUser_PrincipalNull() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(null);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findByFullName("John"));
      assertEquals("No authenticated user found", exception.getMessage());
    }
  }
}