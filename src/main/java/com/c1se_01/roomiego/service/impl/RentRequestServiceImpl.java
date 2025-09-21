package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.RentRequestCreateRequest;
import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.dto.RentRequestUpdateRequest;
import com.c1se_01.roomiego.enums.NotificationType;
import com.c1se_01.roomiego.enums.RentRequestStatus;
import com.c1se_01.roomiego.mapper.RentRequestMapper;
import com.c1se_01.roomiego.model.Notification;
import com.c1se_01.roomiego.model.RentRequest;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RentRequestRepository;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.NotificationService;
import com.c1se_01.roomiego.service.RentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentRequestServiceImpl implements RentRequestService {
    private final RentRequestRepository rentRequestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RentRequestMapper rentRequestMapper;
    private final NotificationService notificationService;

    @Override
    public RentRequestResponse createRentRequest(RentRequestCreateRequest request) {
        User tenant = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RentRequest rentRequest = new RentRequest();
        rentRequest.setTenant(tenant);
        rentRequest.setRoom(room);

        RentRequest saved = rentRequestRepository.save(rentRequest);

        // Gửi WebSocket notification cho chủ phòng
        NotificationDto notificationDto = new NotificationDto();
        Long ownerId = room.getOwner().getId();
        if (ownerId == null) {
            throw new RuntimeException("Owner ID is null");
        }
        notificationDto.setUserId(ownerId);
        notificationDto.setMessage("Bạn có yêu cầu thuê phòng mới từ " + (tenant.getFullName() != null ? tenant.getFullName() : "Unknown"));
        notificationDto.setType(NotificationType.RENT_REQUEST_CREATED);

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        try {
            System.out.println("Sending notification to topic: /topic/notifications/" + ownerId);
            messagingTemplate.convertAndSend("/topic/notifications/" + ownerId, notificationDto);
            System.out.println("Notification sent successfully to topic: /topic/notifications/" + ownerId);
        } catch (Exception e) {
            System.err.println("Failed to send notification to topic: /topic/notifications/" + ownerId + ", Error: " + e.getMessage());
            throw new RuntimeException("Failed to send notification", e);
        }

        return rentRequestMapper.toDto(saved);
    }

    @Override
    public RentRequestResponse updateRentRequestStatus(Long requestId, RentRequestUpdateRequest updateRequest) {
        RentRequest rentRequest = rentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rent request not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!rentRequest.getRoom().getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this room");
        }

        rentRequest.setStatus(updateRequest.getStatus());
        RentRequest updated = rentRequestRepository.save(rentRequest);

        // Gửi thông báo WebSocket cho tenant
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(rentRequest.getTenant().getId());
        notificationDto.setMessage(updateRequest.getStatus() == RentRequestStatus.APPROVED
                ? "Yêu cầu thuê của bạn đã được chấp nhận!"
                : "Yêu cầu thuê của bạn đã bị từ chối.");
        notificationDto.setType(updateRequest.getStatus() == RentRequestStatus.APPROVED
                ? NotificationType.RENT_REQUEST_APPROVED
                : NotificationType.RENT_REQUEST_REJECTED);

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        messagingTemplate.convertAndSend("/topic/notifications/" + rentRequest.getTenant().getId(), notificationDto);

        return rentRequestMapper.toDto(updated);
    }

    @Override
    public RentRequestResponse confirmViewing(Long requestId) {
        RentRequest rentRequest = rentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rent request not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!rentRequest.getTenant().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the tenant of this request");
        }

        if (rentRequest.getStatus() != RentRequestStatus.APPROVED) {
            throw new RuntimeException("Rent request must be APPROVED before confirming viewing");
        }

        rentRequest.setStatus(RentRequestStatus.VIEW_CONFIRMED);
        RentRequest updated = rentRequestRepository.save(rentRequest);

        // Gửi WebSocket thông báo cho chủ phòng
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(rentRequest.getRoom().getOwner().getId());
        notificationDto.setMessage("Người thuê đã xác nhận đi xem phòng!");
        notificationDto.setType(NotificationType.TENANT_CONFIRMED_VIEWING);
        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        messagingTemplate.convertAndSend("/topic/notifications/" + rentRequest.getRoom().getOwner().getId(), notificationDto);

        return rentRequestMapper.toDto(updated);
    }

    @Override
    public RentRequestResponse confirmFinalize(Long requestId) {
        RentRequest rentRequest = rentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rent request not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!rentRequest.getTenant().getId().equals(currentUser.getId()) &&
                !rentRequest.getRoom().getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to finalize this request");
        }

        if (rentRequest.getStatus() != RentRequestStatus.VIEW_CONFIRMED) {
            throw new RuntimeException("Rent request must be VIEW_CONFIRMED before finalizing");
        }

        // Đánh dấu đã xác nhận
        if (currentUser.getId().equals(rentRequest.getTenant().getId())) {
            rentRequest.setTenantFinalize(true);
        }
        if (currentUser.getId().equals(rentRequest.getRoom().getOwner().getId())) {
            rentRequest.setOwnerFinalize(true);
        }

        // Khi đủ cả 2 bên đồng ý thì đổi trạng thái
        if (Boolean.TRUE.equals(rentRequest.getTenantFinalize()) && Boolean.TRUE.equals(rentRequest.getOwnerFinalize())) {
            rentRequest.setStatus(RentRequestStatus.BOTH_FINALIZED);
        }

        RentRequest updated = rentRequestRepository.save(rentRequest);
        return rentRequestMapper.toDto(updated);
    }

    @Override
    public List<RentRequestResponse> getRequestsByOwner() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RentRequest> requests = rentRequestRepository.findByRoomOwnerId(currentUser.getId());
        
        // Log để debug
        System.out.println("Current user ID: " + currentUser.getId());
        System.out.println("Found rent requests: " + requests.size());
        
        return requests.stream()
                .map(request -> {
                    RentRequestResponse dto = rentRequestMapper.toDto(request);
                    // Log để debug
                    System.out.println("Processing rent request: " + request.getId());
                    System.out.println("Room ID: " + request.getRoom().getId());
                    System.out.println("Tenant ID: " + request.getTenant().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RentRequestResponse cancelRental(Long requestId) {
        RentRequest rentRequest = rentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rent request not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!rentRequest.getRoom().getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this room");
        }

        if (rentRequest.getStatus() != RentRequestStatus.APPROVED) {
            throw new RuntimeException("Can only cancel APPROVED rental requests");
        }

        rentRequest.setStatus(RentRequestStatus.REJECTED);
        RentRequest updated = rentRequestRepository.save(rentRequest);

        // Send notification to tenant
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(rentRequest.getTenant().getId());
        notificationDto.setMessage("Chủ phòng đã hủy cho thuê phòng");
        notificationDto.setType(NotificationType.RENT_REQUEST_REJECTED);

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        messagingTemplate.convertAndSend("/topic/notifications/" + rentRequest.getTenant().getId(), notificationDto);

        return rentRequestMapper.toDto(updated);
    }

}
