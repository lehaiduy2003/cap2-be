package com.c1se_01.roomiego.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class UserHandshakeHandlerTest {

  @Mock
  private ServerHttpRequest nonServletRequest;

  @Mock
  private ServletServerHttpRequest servletRequest;

  @Mock
  private HttpServletRequest httpRequest;

  @Mock
  private WebSocketHandler wsHandler;

  private UserHandshakeHandler handler;
  private Map<String, Object> attributes;

  @BeforeEach
  void setUp() {
    handler = new UserHandshakeHandler();
    attributes = Map.of();
  }

  @Test
  void testDetermineUser_WithValidUsername() {
    // Arrange
    when(servletRequest.getServletRequest()).thenReturn(httpRequest);
    when(httpRequest.getParameter("username")).thenReturn("testuser");

    // Act
    Principal principal = handler.determineUser(servletRequest, wsHandler, attributes);

    // Assert
    assertNotNull(principal);
    assertEquals("testuser", principal.getName());
  }

  @Test
  void testDetermineUser_WithNullUsername() {
    // Arrange
    when(servletRequest.getServletRequest()).thenReturn(httpRequest);
    when(httpRequest.getParameter("username")).thenReturn(null);

    // Act
    Principal principal = handler.determineUser(servletRequest, wsHandler, attributes);

    // Assert
    assertNotNull(principal);
    String name = principal.getName();
    assertNotNull(name);
    assertEquals(36, name.length()); // UUID string length
    assertNotEquals("testuser", name);
  }

  @Test
  void testDetermineUser_WithBlankUsername() {
    // Arrange
    when(servletRequest.getServletRequest()).thenReturn(httpRequest);
    when(httpRequest.getParameter("username")).thenReturn("   ");

    // Act
    Principal principal = handler.determineUser(servletRequest, wsHandler, attributes);

    // Assert
    assertNotNull(principal);
    String name = principal.getName();
    assertNotNull(name);
    assertEquals(36, name.length()); // UUID string length
    assertNotEquals("testuser", name);
  }

  @Test
  void testDetermineUser_WithEmptyUsername() {
    // Arrange
    when(servletRequest.getServletRequest()).thenReturn(httpRequest);
    when(httpRequest.getParameter("username")).thenReturn("");

    // Act
    Principal principal = handler.determineUser(servletRequest, wsHandler, attributes);

    // Assert
    assertNotNull(principal);
    String name = principal.getName();
    assertNotNull(name);
    assertEquals(36, name.length()); // UUID string length
    assertNotEquals("testuser", name);
  }

  @Test
  void testDetermineUser_NonServletRequest() {
    // Act
    Principal principal = handler.determineUser(nonServletRequest, wsHandler, attributes);

    // Assert
    assertNotNull(principal);
    String name = principal.getName();
    assertNotNull(name);
    assertEquals(36, name.length()); // UUID string length
  }
}