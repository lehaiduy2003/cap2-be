package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.dto.ViewRespondDTO;
import com.c1se_01.roomiego.enums.ViewRequestStatus;
import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.mapper.ViewRequestMapper;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.model.ViewRequest;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.repository.ViewRequestRepository;
import com.c1se_01.roomiego.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewRequestServiceImplTest {

  @Mock
  private ViewRequestRepository viewRequestRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ViewRequestMapper viewRequestMapper;

  @Mock
  private NotificationService notificationService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private ViewRequestServiceImpl viewRequestService;

  private User owner;
  private User renter;
  private Room room;
  private ViewRequest viewRequest;
  private ViewRequestCreateDTO createDTO;
  private ViewRespondDTO respondDTO;
  private ViewRequestDTO viewRequestDTO;

  @BeforeEach
  void setUp() {
    owner = new User();
    owner.setId(1L);
    owner.setEmail("owner@example.com");
    owner.setFullName("Owner Name");
    owner.setPhone("123456789");

    renter = new User();
    renter.setId(2L);
    renter.setEmail("renter@example.com");
    renter.setFullName("Renter Name");

    room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    viewRequest = new ViewRequest();
    viewRequest.setId(1L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setStatus(ViewRequestStatus.PENDING);
    viewRequest.setOwnerId(owner.getId());
    viewRequest.setCreatedAt(new Date());

    createDTO = new ViewRequestCreateDTO();
    createDTO.setRoomId(1L);

    respondDTO = new ViewRespondDTO();
    respondDTO.setRequestId(1L);
    respondDTO.setAccept(true);
    respondDTO.setAdminNote("Approved");

    viewRequestDTO = new ViewRequestDTO();
    viewRequestDTO.setId(1L);
  }

  @Test
  void createRequest_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(renter.getEmail());
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(userRepository.findByEmail(renter.getEmail())).thenReturn(Optional.of(renter));
      when(viewRequestMapper.toEntity(createDTO)).thenReturn(viewRequest);
      when(viewRequestRepository.save(viewRequest)).thenReturn(viewRequest);
      when(viewRequestMapper.toDTO(viewRequest)).thenReturn(viewRequestDTO);

      ViewRequestDTO result = viewRequestService.createRequest(createDTO);

      assertEquals(viewRequestDTO, result);
      verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + owner.getId()),
          any(NotificationDto.class));
    }
  }

  @Test
  void createRequest_RoomNotFound() {
    when(roomRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> viewRequestService.createRequest(createDTO));
    assertEquals("Room not found", exception.getMessage());
  }

  @Test
  void createRequest_UserNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(renter.getEmail());
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(userRepository.findByEmail(renter.getEmail())).thenReturn(Optional.empty());

      NotFoundException exception = assertThrows(NotFoundException.class,
          () -> viewRequestService.createRequest(createDTO));
      assertEquals("User not found", exception.getMessage());
    }
  }

  @Test
  void getRequestsByOwner_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(viewRequestRepository.findByOwnerId(owner.getId())).thenReturn(Arrays.asList(viewRequest));
      when(viewRequestMapper.toDTO(viewRequest)).thenReturn(viewRequestDTO);

      List<ViewRequestDTO> result = viewRequestService.getRequestsByOwner();

      assertEquals(1, result.size());
      assertEquals(viewRequestDTO, result.get(0));
    }
  }

  @Test
  void respondToRequest_AcceptSuccess() {
    respondDTO.setAccept(true);
    viewRequest.setStatus(ViewRequestStatus.PENDING);

    when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));
    when(viewRequestRepository.save(viewRequest)).thenReturn(viewRequest);
    when(viewRequestMapper.toDTO(viewRequest)).thenReturn(viewRequestDTO);

    ViewRequestDTO result = viewRequestService.respondToRequest(respondDTO);

    assertEquals(viewRequestDTO, result);
    assertEquals(ViewRequestStatus.ACCEPTED, viewRequest.getStatus());
    verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
    verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + renter.getId()),
        any(NotificationDto.class));
  }

  @Test
  void respondToRequest_RejectSuccess() {
    respondDTO.setAccept(false);
    viewRequest.setStatus(ViewRequestStatus.PENDING);

    when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));
    when(viewRequestRepository.save(viewRequest)).thenReturn(viewRequest);
    when(viewRequestMapper.toDTO(viewRequest)).thenReturn(viewRequestDTO);

    ViewRequestDTO result = viewRequestService.respondToRequest(respondDTO);

    assertEquals(viewRequestDTO, result);
    assertEquals(ViewRequestStatus.REJECTED, viewRequest.getStatus());
    verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
    verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + renter.getId()),
        any(NotificationDto.class));
  }

  @Test
  void respondToRequest_RequestNotFound() {
    when(viewRequestRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> viewRequestService.respondToRequest(respondDTO));
    assertEquals("Request not found", exception.getMessage());
  }

  @Test
  void respondToRequest_RequestAlreadyProcessed() {
    viewRequest.setStatus(ViewRequestStatus.ACCEPTED);

    when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> viewRequestService.respondToRequest(respondDTO));
    assertEquals("Request already processed", exception.getMessage());
  }

  @Test
  void cancelRental_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      viewRequest.setStatus(ViewRequestStatus.ACCEPTED);

      when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));
      when(viewRequestRepository.save(viewRequest)).thenReturn(viewRequest);
      when(viewRequestMapper.toDTO(viewRequest)).thenReturn(viewRequestDTO);

      ViewRequestDTO result = viewRequestService.cancelRental(respondDTO);

      assertEquals(viewRequestDTO, result);
      assertEquals(ViewRequestStatus.REJECTED, viewRequest.getStatus());
      verify(notificationService, times(1)).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + renter.getId()),
          any(NotificationDto.class));
    }
  }

  @Test
  void cancelRental_RequestNotFound() {
    when(viewRequestRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> viewRequestService.cancelRental(respondDTO));
    assertEquals("Request not found", exception.getMessage());
  }

  @Test
  void cancelRental_NotOwner() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      User anotherUser = new User();
      anotherUser.setId(3L);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(anotherUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> viewRequestService.cancelRental(respondDTO));
      assertEquals("You are not the owner of this room", exception.getMessage());
    }
  }

  @Test
  void cancelRental_RequestNotAccepted() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      viewRequest.setStatus(ViewRequestStatus.PENDING);

      when(viewRequestRepository.findById(1L)).thenReturn(Optional.of(viewRequest));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> viewRequestService.cancelRental(respondDTO));
      assertEquals("Can only cancel ACCEPTED view requests", exception.getMessage());
    }
  }
}