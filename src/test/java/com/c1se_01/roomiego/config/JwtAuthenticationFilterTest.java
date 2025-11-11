package com.c1se_01.roomiego.config;

import com.c1se_01.roomiego.service.impl.JwtService;
import com.c1se_01.roomiego.service.impl.OurUsersDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private OurUsersDetailService ourUsersDetailService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private UserDetails userDetails;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    // Clear security context before each test
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_NoAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn(null);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, ourUsersDetailService);
  }

  @Test
  void doFilterInternal_BlankAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("");

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, ourUsersDetailService);
  }

  @Test
  void doFilterInternal_AuthorizationHeaderWithSpaces_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("   ");

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, ourUsersDetailService);
  }

  @Test
  void doFilterInternal_ValidTokenButNullUsername_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    String token = "Bearer validToken";
    when(request.getHeader("Authorization")).thenReturn(token);
    when(jwtService.extractUsername("validToken")).thenReturn(null);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername("validToken");
    verifyNoInteractions(ourUsersDetailService);
  }

  @Test
  void doFilterInternal_AlreadyAuthenticated_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    String token = "Bearer validToken";
    String email = "user@example.com";
    when(request.getHeader("Authorization")).thenReturn(token);
    when(jwtService.extractUsername("validToken")).thenReturn(email);

    SecurityContext mockContext = mock(SecurityContext.class);
    when(mockContext.getAuthentication()).thenReturn(mock(UsernamePasswordAuthenticationToken.class)); // already
                                                                                                       // authenticated

    try (MockedStatic<SecurityContextHolder> mocked = Mockito.mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(mockContext);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(request, response);
      verify(jwtService).extractUsername("validToken");
      verifyNoInteractions(ourUsersDetailService);
      verify(mockContext, never()).setAuthentication(any());
    }
  }

  @Test
  void doFilterInternal_InvalidToken_ShouldContinueFilterChain() throws ServletException, IOException {
    // Arrange
    String token = "Bearer invalidToken";
    String email = "user@example.com";
    when(request.getHeader("Authorization")).thenReturn(token);
    when(jwtService.extractUsername("invalidToken")).thenReturn(email);
    when(ourUsersDetailService.loadUserByUsername(email)).thenReturn(userDetails);
    when(jwtService.isTokenValid("invalidToken", userDetails)).thenReturn(false);

    SecurityContext mockContext = mock(SecurityContext.class);
    when(mockContext.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mocked = Mockito.mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(mockContext);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(request, response);
      verify(jwtService).extractUsername("invalidToken");
      verify(ourUsersDetailService).loadUserByUsername(email);
      verify(jwtService).isTokenValid("invalidToken", userDetails);
      verify(mockContext, never()).setAuthentication(any());
    }
  }

  @Test
  void doFilterInternal_ValidToken_ShouldSetAuthentication() throws ServletException, IOException {
    // Arrange
    String token = "Bearer validToken";
    String email = "user@example.com";
    when(request.getHeader("Authorization")).thenReturn(token);
    when(jwtService.extractUsername("validToken")).thenReturn(email);
    when(ourUsersDetailService.loadUserByUsername(email)).thenReturn(userDetails);
    when(jwtService.isTokenValid("validToken", userDetails)).thenReturn(true);
    when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

    SecurityContext mockContext = mock(SecurityContext.class);
    when(mockContext.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mocked = Mockito.mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(mockContext);
      mocked.when(SecurityContextHolder::createEmptyContext).thenReturn(mockContext);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(request, response);
      verify(jwtService).extractUsername("validToken");
      verify(ourUsersDetailService).loadUserByUsername(email);
      verify(jwtService).isTokenValid("validToken", userDetails);
      verify(mockContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
    }
  }
}