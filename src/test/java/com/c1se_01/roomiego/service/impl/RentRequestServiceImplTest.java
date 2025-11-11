package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.RentRequestCreateRequest;
import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.dto.RentRequestUpdateRequest;
import com.c1se_01.roomiego.enums.RentRequestStatus;
import com.c1se_01.roomiego.mapper.RentRequestMapper;
import com.c1se_01.roomiego.model.RentRequest;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RentRequestRepository;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RentRequestServiceImplTest {

  @Mock
  private RentRequestRepository rentRequestRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private RentRequestMapper rentRequestMapper;

  @Mock
  private NotificationService notificationService;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private RentRequestServiceImpl rentRequestService;

  private User tenant;
  private User owner;
  private Room room;
  private RentRequest rentRequest;
  private RentRequestCreateRequest createRequest;
  private RentRequestUpdateRequest updateRequestApproved;
  private RentRequestUpdateRequest updateRequestRejected;
  private RentRequestResponse response;

  @BeforeEach
  void setUp() {
    tenant = new User();
    tenant.setId(1L);
    tenant.setFullName("Tenant Name");

    owner = new User();
    owner.setId(2L);

    room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    rentRequest = new RentRequest();
    rentRequest.setId(1L);
    rentRequest.setTenant(tenant);
    rentRequest.setRoom(room);
    rentRequest.setStatus(RentRequestStatus.PENDING);

    createRequest = new RentRequestCreateRequest();
    createRequest.setRoomId(1L);

    updateRequestApproved = new RentRequestUpdateRequest();
    updateRequestApproved.setStatus(RentRequestStatus.APPROVED);

    updateRequestRejected = new RentRequestUpdateRequest();
    updateRequestRejected.setStatus(RentRequestStatus.REJECTED);
  }

  @Test
  void createRentRequest_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      when(roomRepository.findById(eq(1L))).thenReturn(Optional.of(room));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.createRentRequest(createRequest);
      assertEquals(response, result);
      verify(roomRepository).findById(eq(1L));
      ArgumentCaptor<RentRequest> captor = ArgumentCaptor.forClass(RentRequest.class);
      verify(rentRequestRepository).save(captor.capture());
      assertEquals(tenant, captor.getValue().getTenant());
      assertEquals(room, captor.getValue().getRoom());
      verify(notificationService).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate).convertAndSend(eq("/topic/notifications/2"), any(NotificationDto.class));
    }
  }

  @Test
  void createRentRequest_RoomNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(roomRepository.findById(eq(1L))).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> rentRequestService.createRentRequest(createRequest));
      assertEquals("Room not found", exception.getMessage());
    }
  }

  @Test
  void createRentRequest_OwnerIdNull() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      Room roomWithNullOwnerId = new Room();
      roomWithNullOwnerId.setId(1L);
      User ownerNull = new User();
      roomWithNullOwnerId.setOwner(ownerNull); // id null

      when(roomRepository.findById(eq(1L))).thenReturn(Optional.of(roomWithNullOwnerId));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> rentRequestService.createRentRequest(createRequest));
      assertEquals("Owner ID is null", exception.getMessage());
    }
  }

  @Test
  void updateRentRequestStatus_Approved_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.updateRentRequestStatus(1L, updateRequestApproved);
      assertEquals(response, result);
      verify(rentRequestRepository).findById(eq(1L));
      verify(rentRequestRepository).save(rentRequest);
      assertEquals(RentRequestStatus.APPROVED, rentRequest.getStatus());
      verify(notificationService).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate).convertAndSend(eq("/topic/notifications/1"), any(NotificationDto.class));
    }
  }

  @Test
  void updateRentRequestStatus_Rejected_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.updateRentRequestStatus(1L, updateRequestRejected);
      assertEquals(response, result);
      verify(rentRequestRepository).findById(eq(1L));
      verify(rentRequestRepository).save(rentRequest);
      assertEquals(RentRequestStatus.REJECTED, rentRequest.getStatus());
      verify(notificationService).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate).convertAndSend(eq("/topic/notifications/1"), any(NotificationDto.class));
    }
  }

  @Test
  void updateRentRequestStatus_RequestNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> rentRequestService.updateRentRequestStatus(1L, updateRequestApproved));
      assertEquals("Rent request not found", exception.getMessage());
    }
  }

  @Test
  void updateRentRequestStatus_NotOwner() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> rentRequestService.updateRentRequestStatus(1L, updateRequestApproved));
      assertEquals("You are not the owner of this room", exception.getMessage());
    }
  }

  @Test
  void confirmViewing_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      rentRequest.setStatus(RentRequestStatus.APPROVED);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.confirmViewing(1L);
      assertEquals(response, result);
      assertEquals(RentRequestStatus.VIEW_CONFIRMED, rentRequest.getStatus());
      verify(notificationService).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate).convertAndSend(eq("/topic/notifications/2"), any(NotificationDto.class));
    }
  }

  @Test
  void confirmViewing_RequestNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmViewing(1L));
      assertEquals("Rent request not found", exception.getMessage());
    }
  }

  @Test
  void confirmViewing_NotTenant() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmViewing(1L));
      assertEquals("You are not the tenant of this request", exception.getMessage());
    }
  }

  @Test
  void confirmViewing_StatusNotApproved() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      rentRequest.setStatus(RentRequestStatus.PENDING);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmViewing(1L));
      assertEquals("Rent request must be APPROVED before confirming viewing", exception.getMessage());
    }
  }

  @Test
  void confirmFinalize_TenantFinalize() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      rentRequest.setStatus(RentRequestStatus.VIEW_CONFIRMED);
      rentRequest.setTenantFinalize(false);
      rentRequest.setOwnerFinalize(false);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.confirmFinalize(1L);

      assertEquals(response, result);
      assertTrue(rentRequest.getTenantFinalize());
      assertFalse(rentRequest.getOwnerFinalize());
      assertEquals(RentRequestStatus.VIEW_CONFIRMED, rentRequest.getStatus());
    }
  }

  @Test
  void confirmFinalize_OwnerFinalize() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      rentRequest.setStatus(RentRequestStatus.VIEW_CONFIRMED);
      rentRequest.setTenantFinalize(false);
      rentRequest.setOwnerFinalize(false);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.confirmFinalize(1L);

      assertEquals(response, result);
      assertFalse(rentRequest.getTenantFinalize());
      assertTrue(rentRequest.getOwnerFinalize());
      assertEquals(RentRequestStatus.VIEW_CONFIRMED, rentRequest.getStatus());
    }
  }

  @Test
  void confirmFinalize_BothFinalize() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      rentRequest.setStatus(RentRequestStatus.VIEW_CONFIRMED);
      rentRequest.setTenantFinalize(true);
      rentRequest.setOwnerFinalize(false);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.confirmFinalize(1L);

      assertEquals(response, result);
      assertTrue(rentRequest.getTenantFinalize());
      assertTrue(rentRequest.getOwnerFinalize());
      assertEquals(RentRequestStatus.BOTH_FINALIZED, rentRequest.getStatus());
    }
  }

  @Test
  void confirmFinalize_RequestNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmFinalize(1L));
      assertEquals("Rent request not found", exception.getMessage());
    }
  }

  @Test
  void confirmFinalize_NotAllowed() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      User otherUser = new User();
      otherUser.setId(3L);
      when(authentication.getPrincipal()).thenReturn(otherUser);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmFinalize(1L));
      assertEquals("You are not allowed to finalize this request", exception.getMessage());
    }
  }

  @Test
  void confirmFinalize_StatusNotViewConfirmed() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      rentRequest.setStatus(RentRequestStatus.APPROVED);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.confirmFinalize(1L));
      assertEquals("Rent request must be VIEW_CONFIRMED before finalizing", exception.getMessage());
    }
  }

  @Test
  void getRequestsByOwner_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      List<RentRequest> requests = Arrays.asList(rentRequest);
      when(rentRequestRepository.findByRoomOwnerId(eq(2L))).thenReturn(requests);

      List<RentRequestResponse> result = rentRequestService.getRequestsByOwner();
      assertEquals(1, result.size());
      assertEquals(response, result.get(0));
      verify(rentRequestRepository).findByRoomOwnerId(eq(2L));
    }
  }

  @Test
  void cancelRental_Success() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      lenient().when(rentRequestMapper.toDto(any(RentRequest.class))).thenReturn(response);

      rentRequest.setStatus(RentRequestStatus.APPROVED);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));
      when(rentRequestRepository.save(any(RentRequest.class))).thenReturn(rentRequest);

      RentRequestResponse result = rentRequestService.cancelRental(1L);
      assertEquals(response, result);
      assertEquals(RentRequestStatus.REJECTED, rentRequest.getStatus());
      verify(notificationService).saveNotification(any(NotificationDto.class));
      verify(messagingTemplate).convertAndSend(eq("/topic/notifications/1"), any(NotificationDto.class));
    }
  }

  @Test
  void cancelRental_RequestNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.cancelRental(1L));
      assertEquals("Rent request not found", exception.getMessage());
    }
  }

  @Test
  void cancelRental_NotOwner() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(tenant);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.cancelRental(1L));
      assertEquals("You are not the owner of this room", exception.getMessage());
    }
  }

  @Test
  void cancelRental_StatusNotApproved() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(owner);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      rentRequest.setStatus(RentRequestStatus.PENDING);
      when(rentRequestRepository.findById(eq(1L))).thenReturn(Optional.of(rentRequest));

      RuntimeException exception = assertThrows(RuntimeException.class, () -> rentRequestService.cancelRental(1L));
      assertEquals("Can only cancel APPROVED rental requests", exception.getMessage());
    }
  }
}