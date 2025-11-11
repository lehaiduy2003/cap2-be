package com.c1se_01.roomiego.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.security.Principal;
import java.util.List;

import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

  @Mock
  private UserHandshakeHandler userHandshakeHandler;

  private WebSocketConfig webSocketConfig;

  @BeforeEach
  void setUp() {
    webSocketConfig = new WebSocketConfig(userHandshakeHandler);
  }

  @Test
  void configureMessageBroker_shouldConfigureCorrectly() {
    // Given
    MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

    // When
    webSocketConfig.configureMessageBroker(registry);

    // Then
    verify(registry).enableSimpleBroker("/topic", "/chatroom", "/user");
    verify(registry).setApplicationDestinationPrefixes("/app");
    verify(registry).setUserDestinationPrefix("/user");
  }

  @Test
  void registerStompEndpoints_shouldRegisterApiSocketEndpoint() {
    // Given
    StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
    StompWebSocketEndpointRegistration apiRegistration = mock(StompWebSocketEndpointRegistration.class);
    StompWebSocketEndpointRegistration wsRegistration = mock(StompWebSocketEndpointRegistration.class);
    when(registry.addEndpoint("/api/socket")).thenReturn(apiRegistration);
    when(apiRegistration.setHandshakeHandler(userHandshakeHandler)).thenReturn(apiRegistration);
    when(apiRegistration.setAllowedOrigins("http://localhost:5173", "https://cap2-fe.vercel.app"))
        .thenReturn(apiRegistration);
    when(registry.addEndpoint("/ws")).thenReturn(wsRegistration);
    when(wsRegistration.setHandshakeHandler(userHandshakeHandler)).thenReturn(wsRegistration);
    when(wsRegistration.setAllowedOriginPatterns("*")).thenReturn(wsRegistration);

    // When
    webSocketConfig.registerStompEndpoints(registry);

    // Then
    verify(registry).addEndpoint("/api/socket");
    verify(apiRegistration).setHandshakeHandler(userHandshakeHandler);
    verify(apiRegistration).setAllowedOrigins("http://localhost:5173", "https://cap2-fe.vercel.app");
    verify(apiRegistration).withSockJS();
  }

  @Test
  void registerStompEndpoints_shouldRegisterWsEndpoint() {
    // Given
    StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
    StompWebSocketEndpointRegistration wsRegistration = mock(StompWebSocketEndpointRegistration.class);
    StompWebSocketEndpointRegistration apiRegistration = mock(StompWebSocketEndpointRegistration.class);
    when(registry.addEndpoint("/ws")).thenReturn(wsRegistration);
    when(wsRegistration.setHandshakeHandler(userHandshakeHandler)).thenReturn(wsRegistration);
    when(wsRegistration.setAllowedOriginPatterns("*")).thenReturn(wsRegistration);
    when(registry.addEndpoint("/api/socket")).thenReturn(apiRegistration);
    when(apiRegistration.setHandshakeHandler(userHandshakeHandler)).thenReturn(apiRegistration);
    when(apiRegistration.setAllowedOrigins("http://localhost:5173", "https://cap2-fe.vercel.app"))
        .thenReturn(apiRegistration);

    // When
    webSocketConfig.registerStompEndpoints(registry);

    // Then
    verify(registry).addEndpoint("/ws");
    verify(wsRegistration).setHandshakeHandler(userHandshakeHandler);
    verify(wsRegistration).setAllowedOriginPatterns("*");
    verify(wsRegistration).withSockJS();
  }

  @Test
  void configureWebSocketTransport_shouldSetLimits() {
    // Given
    WebSocketTransportRegistration registry = mock(WebSocketTransportRegistration.class);
    when(registry.setSendTimeLimit(60 * 1000)).thenReturn(registry);
    when(registry.setSendBufferSizeLimit(50 * 1024 * 1024)).thenReturn(registry);
    when(registry.setMessageSizeLimit(50 * 1024 * 1024)).thenReturn(registry);

    // When
    webSocketConfig.configureWebSocketTransport(registry);

    // Then
    verify(registry).setSendTimeLimit(60 * 1000);
    verify(registry).setSendBufferSizeLimit(50 * 1024 * 1024);
    verify(registry).setMessageSizeLimit(50 * 1024 * 1024);
  }

  @Test
  void configureClientInboundChannel_shouldAddInterceptor() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

    // When
    webSocketConfig.configureClientInboundChannel(registration);

    // Then
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();
    assert interceptor != null;
  }

  @Test
  void interceptor_preSend_shouldSetUser_whenConnectAndUsernamePresent() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
    webSocketConfig.configureClientInboundChannel(registration);
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();

    Message<?> message = mock(Message.class);
    StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
    List<String> usernames = List.of("testuser");

    try (MockedStatic<MessageHeaderAccessor> mockedStatic = mockStatic(MessageHeaderAccessor.class)) {
      mockedStatic.when(() -> MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class))
          .thenReturn(accessor);
      when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
      when(accessor.getNativeHeader("username")).thenReturn(usernames);

      MessageChannel channel = mock(MessageChannel.class);

      // When
      Message<?> result = interceptor.preSend(message, channel);

      // Then
      verify(accessor).setUser(any(Principal.class));
      assert result == message;
    }
  }

  @Test
  void interceptor_preSend_shouldNotSetUser_whenUsernameNull() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
    webSocketConfig.configureClientInboundChannel(registration);
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();

    Message<?> message = mock(Message.class);
    StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);

    try (MockedStatic<MessageHeaderAccessor> mockedStatic = mockStatic(MessageHeaderAccessor.class)) {
      mockedStatic.when(() -> MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class))
          .thenReturn(accessor);
      when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
      when(accessor.getNativeHeader("username")).thenReturn(null);

      MessageChannel channel = mock(MessageChannel.class);

      // When
      Message<?> result = interceptor.preSend(message, channel);

      // Then
      verify(accessor, never()).setUser(any(Principal.class));
      assert result == message;
    }
  }

  @Test
  void interceptor_preSend_shouldNotSetUser_whenUsernameEmpty() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
    webSocketConfig.configureClientInboundChannel(registration);
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();

    Message<?> message = mock(Message.class);
    StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
    List<String> usernames = List.of();

    try (MockedStatic<MessageHeaderAccessor> mockedStatic = mockStatic(MessageHeaderAccessor.class)) {
      mockedStatic.when(() -> MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class))
          .thenReturn(accessor);
      when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
      when(accessor.getNativeHeader("username")).thenReturn(usernames);

      MessageChannel channel = mock(MessageChannel.class);

      // When
      Message<?> result = interceptor.preSend(message, channel);

      // Then
      verify(accessor, never()).setUser(any(Principal.class));
      assert result == message;
    }
  }

  @Test
  void interceptor_preSend_shouldNotSetUser_whenCommandNotConnect() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
    webSocketConfig.configureClientInboundChannel(registration);
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();

    Message<?> message = mock(Message.class);
    StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);

    try (MockedStatic<MessageHeaderAccessor> mockedStatic = mockStatic(MessageHeaderAccessor.class)) {
      mockedStatic.when(() -> MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class))
          .thenReturn(accessor);
      when(accessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);

      MessageChannel channel = mock(MessageChannel.class);

      // When
      Message<?> result = interceptor.preSend(message, channel);

      // Then
      verify(accessor, never()).setUser(any(Principal.class));
      assert result == message;
    }
  }

  @Test
  void interceptor_preSend_shouldReturnMessage_whenAccessorNull() {
    // Given
    ChannelRegistration registration = mock(ChannelRegistration.class);
    ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
    webSocketConfig.configureClientInboundChannel(registration);
    verify(registration).interceptors(captor.capture());
    ChannelInterceptor interceptor = captor.getValue();

    Message<?> message = mock(Message.class);

    try (MockedStatic<MessageHeaderAccessor> mockedStatic = mockStatic(MessageHeaderAccessor.class)) {
      mockedStatic.when(() -> MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class)).thenReturn(null);

      MessageChannel channel = mock(MessageChannel.class);

      // When
      Message<?> result = interceptor.preSend(message, channel);

      // Then
      assert result == message;
    }
  }
}