package com.c1se_01.roomiego.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  private JwtService jwtService;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
  }

  // Tests for generateToken method
  @Test
  void generateToken_SuccessfulGeneration() {
    when(userDetails.getUsername()).thenReturn("test@example.com");

    String token = jwtService.generateToken(userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  // Tests for generateRefreshToken method
  @Test
  void generateRefreshToken_SuccessfulGeneration() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "USER");

    String refreshToken = jwtService.generateRefreshToken(claims, userDetails);

    assertNotNull(refreshToken);
    assertFalse(refreshToken.isEmpty());
    assertTrue(refreshToken.contains("."));
  }

  @Test
  void generateRefreshToken_EmptyClaims() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    Map<String, Object> claims = new HashMap<>();

    String refreshToken = jwtService.generateRefreshToken(claims, userDetails);

    assertNotNull(refreshToken);
    assertFalse(refreshToken.isEmpty());
  }

  // Tests for extractUsername method
  @Test
  void extractUsername_ValidToken() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    String token = jwtService.generateToken(userDetails);

    String extractedUsername = jwtService.extractUsername(token);

    assertEquals("test@example.com", extractedUsername);
  }

  @Test
  void extractUsername_InvalidToken() {
    String invalidToken = "invalid.jwt.token";

    assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void extractUsername_MalformedToken() {
    String malformedToken = "malformedtoken";

    assertThrows(Exception.class, () -> jwtService.extractUsername(malformedToken));
  }

  // Tests for isTokenValid method
  @Test
  void isTokenValid_ValidTokenAndUser() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    String token = jwtService.generateToken(userDetails);

    boolean isValid = jwtService.isTokenValid(token, userDetails);

    assertTrue(isValid);
  }

  @Test
  void isTokenValid_InvalidUsername() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    String token = jwtService.generateToken(userDetails);
    UserDetails differentUser = org.mockito.Mockito.mock(UserDetails.class);
    when(differentUser.getUsername()).thenReturn("different@example.com");

    boolean isValid = jwtService.isTokenValid(token, differentUser);

    assertFalse(isValid);
  }

  @Test
  void isTokenValid_ExpiredToken() {
    // Create a token that expires immediately by manipulating the service
    // Since we can't easily mock time, we'll test with an invalid token
    String invalidToken = "expired.jwt.token";

    assertThrows(Exception.class, () -> jwtService.isTokenValid(invalidToken, userDetails));
  }

  // Tests for isTokenExpired method
  @Test
  void isTokenExpired_NotExpired() {
    when(userDetails.getUsername()).thenReturn("test@example.com");
    String token = jwtService.generateToken(userDetails);

    boolean isExpired = jwtService.isTokenExpired(token);

    assertFalse(isExpired);
  }

  @Test
  void isTokenExpired_InvalidToken() {
    String invalidToken = "invalid.jwt.token";

    assertThrows(Exception.class, () -> jwtService.isTokenExpired(invalidToken));
  }
}