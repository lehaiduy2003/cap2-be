package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.*;
import com.c1se_01.roomiego.enums.NotificationType;
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
import com.c1se_01.roomiego.service.ViewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewRequestServiceImpl implements ViewRequestService {
    private final ViewRequestRepository viewRequestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ViewRequestMapper viewRequestMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public ViewRequestDTO createRequest(ViewRequestCreateDTO dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User renter = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ViewRequest viewRequest = viewRequestMapper.toEntity(dto);
        viewRequest.setRoom(room);
        viewRequest.setRenter(renter);
        viewRequest.setStatus(ViewRequestStatus.PENDING);
        viewRequest.setCreatedAt(new Date());
        viewRequest.setOwnerId(room.getOwner().getId());

        ViewRequest savedRequest = viewRequestRepository.save(viewRequest);

        // Gửi WebSocket notification cho chủ phòng
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(room.getOwner().getId());
        notificationDto.setMessage("Bạn có yêu cầu xem phòng mới từ " + renter.getFullName());
        notificationDto.setType(NotificationType.RENT_REQUEST_VIEW_ROOM);

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        System.out.println("Sending notification to topic: /topic/notifications/" + room.getOwner().getId());
        messagingTemplate.convertAndSend("/topic/notifications/" + room.getOwner().getId(), notificationDto);

        return viewRequestMapper.toDTO(savedRequest);
    }

    @Override
    public List<ViewRequestDTO> getRequestsByOwner() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Current user ID: " + currentUser.getId());

        List<ViewRequest> requests = viewRequestRepository.findByOwnerId(currentUser.getId());
        System.out.println("Found view requests: " + requests.size());

        return requests.stream()
                .map(request -> {
                    System.out.println("Processing view request: " + request.getId());
                    System.out.println("Room ID: " + request.getRoom().getId());
                    System.out.println("Renter ID: " + request.getRenter().getId());
                    return viewRequestMapper.toDTO(request);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ViewRequestDTO respondToRequest(ViewRespondDTO viewRespondDTO) {
        ViewRequest request = viewRequestRepository.findById(viewRespondDTO.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (request.getStatus() != ViewRequestStatus.PENDING) {
            throw new IllegalStateException("Request already processed");
        }

        request.setStatus(viewRespondDTO.isAccept() ? ViewRequestStatus.ACCEPTED : ViewRequestStatus.REJECTED);
        request.setAdminNote(viewRespondDTO.getAdminNote());
        ViewRequest updatedRequest = viewRequestRepository.save(request);


        // Send notification to tenant
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(request.getRenter().getId());

        if (request.getStatus() == ViewRequestStatus.ACCEPTED) {
            // Gửi Thông báo cho người thuê
            notificationDto.setMessage("Yêu cầu xem phòng đã được chấp nhận. Liên hệ Zalo: " + request.getRoom().getOwner().getPhone());
            notificationDto.setType(NotificationType.OWNER_APPROVED);

            messagingTemplate.convertAndSend("/topic/notifications/" + request.getRenter().getId(), notificationDto);
        } else {
            // Gửi Thông báo cho người thuê
            notificationDto.setMessage("Yêu cầu xem phòng bị từ chối. Lý do: " + request.getAdminNote());
            notificationDto.setType(NotificationType.OWNER_REJECTED);
            messagingTemplate.convertAndSend("/topic/notifications/" + request.getRenter().getId(), notificationDto);
        }

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        return viewRequestMapper.toDTO(updatedRequest);
    }

    @Override
    public ViewRequestDTO cancelRental(ViewRespondDTO viewRespondDTO) {
        ViewRequest request = viewRequestRepository.findById(viewRespondDTO.getRequestId())
                .orElseThrow(() -> new NotFoundException("Request not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!request.getOwnerId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this room");
        }

        if (request.getStatus() != ViewRequestStatus.ACCEPTED) {
            throw new RuntimeException("Can only cancel ACCEPTED view requests");
        }

        request.setStatus(ViewRequestStatus.REJECTED);
        request.setAdminNote(viewRespondDTO.getAdminNote());
        ViewRequest updatedRequest = viewRequestRepository.save(request);


        // Send notification to tenant
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(request.getRenter().getId());
        notificationDto.setMessage("Yêu cầu xem phòng đã bị hủy. Lý do:" + request.getAdminNote());
        notificationDto.setType(NotificationType.OWNER_REJECTED);
        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        // Gửi thông báo cho người thuê
        messagingTemplate.convertAndSend("/topic/notifications/" + request.getRenter().getId(), notificationDto);

        return viewRequestMapper.toDTO(updatedRequest);
    }
}
