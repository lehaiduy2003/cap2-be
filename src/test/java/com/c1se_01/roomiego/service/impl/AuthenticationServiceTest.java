package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.UserDto;
import com.c1se_01.roomiego.enums.Gender;
import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthenticationService authenticationService;

  private UserDto userDto;
  private User user;

  @BeforeEach
  void setUp() {
    userDto = new UserDto();
    userDto.setEmail("test@example.com");
    userDto.setPassword("password");
    userDto.setFullName("Test User");
    userDto.setRole("OWNER");
    userDto.setGender("MALE");
    userDto.setPhone("123456789");
    userDto.setBio("Test bio");
    userDto.setDob("1990-01-01");

    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user.setFullName("Test User");
    user.setRole(Role.OWNER);
    user.setGender(Gender.MALE);
    user.setPhone("123456789");
    user.setBio("Test bio");
    user.setDob(new Date());
    user.setCreatedAt(LocalDateTime.now());
  }

  // Tests for register method
  @Test
  void register_SuccessfulRegistration() {
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = authenticationService.register(userDto);

    assertEquals(200, result.getStatusCode());
    assertEquals("User Saved Successfully", result.getMessage());
    assertNotNull(result.getUser());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void register_InvalidDateFormat() {
    userDto.setDob("invalid-date");

    UserDto result = authenticationService.register(userDto);

    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("Invalid date format"));
  }

  @Test
  void register_SaveException() {
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB Error"));

    UserDto result = authenticationService.register(userDto);

    assertEquals(500, result.getStatusCode());
    assertTrue(result.getError().contains("DB Error"));
  }

  @Test
  void register_NullDob() {
    userDto.setDob(null);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = authenticationService.register(userDto);

    assertEquals(200, result.getStatusCode());
    verify(userRepository, times(1)).save(any(User.class));
  }

  // Tests for login method
  @Test
  void login_SuccessfulLogin() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
    when(jwtService.generateRefreshToken(any(), any(User.class))).thenReturn("refreshToken");

    UserDto result = authenticationService.login(userDto);

    assertEquals(200, result.getStatusCode());
    assertEquals("Successfully Logged In", result.getMessage());
    assertEquals("jwtToken", result.getToken());
    assertEquals("refreshToken", result.getRefreshToken());
    assertEquals("24Hrs", result.getExpirationTime());
  }

  @Test
  void login_AuthenticationFailure() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    UserDto result = authenticationService.login(userDto);

    assertEquals(500, result.getStatusCode());
    assertEquals("Bad credentials", result.getMessage());
  }

  @Test
  void login_UserNotFound() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    UserDto result = authenticationService.login(userDto);

    assertEquals(500, result.getStatusCode());
    assertTrue(result.getMessage().contains("User not found"));
  }

  // Tests for refreshToken method
  @Test
  void refreshToken_ValidToken() {
    when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
    when(jwtService.generateToken(any(User.class))).thenReturn("newJwtToken");

    UserDto refreshRequest = new UserDto();
    refreshRequest.setToken("refreshToken");

    UserDto result = authenticationService.refreshToken(refreshRequest);

    assertEquals(200, result.getStatusCode());
    assertEquals("Successfully Refreshed Token", result.getMessage());
    assertEquals("newJwtToken", result.getToken());
    assertEquals("refreshToken", result.getRefreshToken());
  }

  @Test
  void refreshToken_InvalidToken() {
    when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(false);

    UserDto refreshRequest = new UserDto();
    refreshRequest.setToken("invalidToken");

    UserDto result = authenticationService.refreshToken(refreshRequest);

    assertEquals(200, result.getStatusCode());
    // Note: The method sets status 200 even if token invalid, but doesn't set new
    // token
  }

  @Test
  void refreshToken_UserNotFound() {
    when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    UserDto refreshRequest = new UserDto();
    refreshRequest.setToken("refreshToken");

    UserDto result = authenticationService.refreshToken(refreshRequest);

    assertEquals(500, result.getStatusCode());
    assertTrue(result.getMessage().contains("No value present"));
  }

  // Tests for getAllUsers method
  @Test
  void getAllUsers_UsersFound() {
    List<User> users = Arrays.asList(user);
    when(userRepository.findAll()).thenReturn(users);

    UserDto result = authenticationService.getAllUsers();

    assertEquals(200, result.getStatusCode());
    assertEquals("Successful", result.getMessage());
    assertEquals(users, result.getUsersList());
  }

  @Test
  void getAllUsers_NoUsers() {
    when(userRepository.findAll()).thenReturn(Collections.emptyList());

    UserDto result = authenticationService.getAllUsers();

    assertEquals(404, result.getStatusCode());
    assertEquals("No users found", result.getMessage());
  }

  // Tests for getUsersById method
  @Test
  void getUsersById_UserFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserDto result = authenticationService.getUsersById(1L);

    assertEquals(200, result.getStatusCode());
    assertEquals("Users with id '1' found successfully", result.getMessage());
    assertNotNull(result.getUsersList());
    assertEquals(1, result.getUsersList().size());
  }

  @Test
  void getUsersById_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    UserDto result = authenticationService.getUsersById(1L);

    assertEquals(500, result.getStatusCode());
    assertTrue(result.getMessage().contains("User Not found"));
  }

  // Tests for deleteUser method
  @Test
  void deleteUser_SuccessfulDelete() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserDto result = authenticationService.deleteUser(1L);

    assertEquals(200, result.getStatusCode());
    assertEquals("User deleted successfully", result.getMessage());
    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteUser_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    UserDto result = authenticationService.deleteUser(1L);

    assertEquals(404, result.getStatusCode());
    assertEquals("User not found for deletion", result.getMessage());
  }

  // Tests for updateUser method
  @Test
  void updateUser_SuccessfulUpdate() {
    User updatedUser = new User();
    updatedUser.setEmail("newemail@example.com");
    updatedUser.setFullName("New Name");
    updatedUser.setRole(Role.RENTER);
    updatedUser.setPhone("987654321");
    updatedUser.setBio("New bio");
    updatedUser.setDob(new Date());
    updatedUser.setGender(Gender.FEMALE);
    updatedUser.setPassword(""); // Empty password, so not updated

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = authenticationService.updateUser(1L, updatedUser);

    assertEquals(200, result.getStatusCode());
    assertEquals("User updated successfully", result.getMessage());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void updateUser_UserNotFound() {
    User updatedUser = new User();
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    UserDto result = authenticationService.updateUser(1L, updatedUser);

    assertEquals(404, result.getStatusCode());
    assertEquals("User not found for update", result.getMessage());
  }

  @Test
  void updateUser_WithPasswordUpdate() {
    User updatedUser = new User();
    updatedUser.setPassword("newPassword");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = authenticationService.updateUser(1L, updatedUser);

    assertEquals(200, result.getStatusCode());
    verify(passwordEncoder, times(1)).encode("newPassword");
    verify(userRepository, times(1)).save(any(User.class));
  }

  // Tests for getMyInfo method
  @Test
  void getMyInfo_UserFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    UserDto result = authenticationService.getMyInfo("test@example.com");

    assertEquals(200, result.getStatusCode());
    assertEquals("successful", result.getMessage());
    assertEquals(user.getId(), result.getId());
    assertEquals(user.getEmail(), result.getEmail());
  }

  @Test
  void getMyInfo_UserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    UserDto result = authenticationService.getMyInfo("test@example.com");

    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getMessage());
  }

  // Tests for getUserByEmail method
  @Test
  void getUserByEmail_UserFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    UserDto result = authenticationService.getUserByEmail("test@example.com");

    assertEquals(200, result.getStatusCode());
    assertEquals("successful", result.getMessage());
    assertEquals(user.getId(), result.getId());
    assertEquals(user.getEmail(), result.getEmail());
  }

  @Test
  void getUserByEmail_UserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    UserDto result = authenticationService.getUserByEmail("test@example.com");

    assertEquals(404, result.getStatusCode());
    assertEquals("User not found", result.getMessage());
  }
}