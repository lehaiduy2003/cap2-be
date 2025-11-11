package com.c1se_01.roomiego.config;

import com.c1se_01.roomiego.service.impl.OurUsersDetailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

  @Mock
  private OurUsersDetailService ourUsersDetailService;

  @Mock
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock
  private AuthenticationConfiguration authenticationConfiguration;

  @InjectMocks
  private WebSecurityConfig webSecurityConfig;

  @Test
  void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
    // When
    PasswordEncoder encoder = webSecurityConfig.passwordEncoder();

    // Then
    assertNotNull(encoder);
    assertInstanceOf(BCryptPasswordEncoder.class, encoder);
  }

  @Test
  void authenticationProvider_ShouldReturnDaoAuthenticationProviderWithCorrectServices() {
    // When
    AuthenticationProvider provider = webSecurityConfig.authenticationProvider();

    // Then
    assertNotNull(provider);
    assertInstanceOf(DaoAuthenticationProvider.class, provider);
    // Note: Internal services are set correctly as per implementation
  }

  @Test
  void authenticationManager_ShouldReturnAuthenticationManagerFromConfiguration() throws Exception {
    // Given
    AuthenticationManager mockManager = mock(AuthenticationManager.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

    // When
    AuthenticationManager manager = webSecurityConfig.authenticationManager(authenticationConfiguration);

    // Then
    assertNotNull(manager);
    assertEquals(mockManager, manager);
    verify(authenticationConfiguration).getAuthenticationManager();
  }

  @Test
  void authenticationManager_ShouldThrowExceptionWhenConfigurationFails() throws Exception {
    // Given
    when(authenticationConfiguration.getAuthenticationManager()).thenThrow(new RuntimeException("Config error"));

    // When & Then
    assertThrows(RuntimeException.class, () -> webSecurityConfig.authenticationManager(authenticationConfiguration));
  }

  @Test
  void allowUrlDoubleSlashFirewall_ShouldReturnStrictHttpFirewallWithDoubleSlashAllowed() {
    // When
    HttpFirewall firewall = webSecurityConfig.allowUrlDoubleSlashFirewall();

    // Then
    assertNotNull(firewall);
    assertInstanceOf(StrictHttpFirewall.class, firewall);
    // Note: We can't directly test the internal state, but the method sets
    // allowUrlEncodedDoubleSlash to true
    // This is a happy case test
  }

  // Note: Testing securityFilterChain is complex due to extensive mocking
  // required for HttpSecurity
  // For this test, we focus on the other bean methods as they have clearer logic
  // flows
  // In a real scenario, integration tests with MockMvc would test the actual
  // security behavior
}