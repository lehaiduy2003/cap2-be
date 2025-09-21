package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.common.PageCustom;
import com.c1se_01.roomiego.common.PageableCustom;
import com.c1se_01.roomiego.common.ResponseData;
import com.c1se_01.roomiego.dto.HandleReportRequest;
import com.c1se_01.roomiego.dto.NotificationDto;
import com.c1se_01.roomiego.dto.ReportRequest;
import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.enums.NotificationType;
import com.c1se_01.roomiego.enums.RentRequestStatus;
import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.mapper.RentRequestMapper;
import com.c1se_01.roomiego.model.Report;
import com.c1se_01.roomiego.mapper.ReportMapper;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.ReportRepository;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.NotificationService;
import com.c1se_01.roomiego.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final RoomRepository roomRepository;
    private final ReportMapper reportMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Override
    public void reportRoom(ReportRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User reporter = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Report report = Report.builder()
                .room(room)
                .reporter(reporter)
                .reason(request.getReason())
                .createdAt(new Date())
                .isHandled(false)
                .build();
        reportRepository.save(report);

        // Gửi thông báo WebSocket cho tenant
        User user = userRepository.findByRole(Role.ADMIN).orElse(null);

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUserId(user.getId());
        notificationDto.setMessage("Bạn có bài báo cáo phòng từ: " + reporter.getFullName());
        notificationDto.setType(NotificationType.BREACH);

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);

        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationDto);
    }

    @Override
    public ResponseData getReports(Boolean isHandled, Pageable pageable) {
        Page<Report> entities;

        if (isHandled == null) {
            entities = reportRepository.findAll(pageable); // không lọc gì
        } else {
            entities = reportRepository.findAllByIsHandled(isHandled, pageable)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
        }

        List<Report> reports = entities.getContent();
        List<ReportResponse> reportResponses = reportMapper.toDto(reports);
        Page<ReportResponse> page = new PageImpl<>(reportResponses, pageable, entities.getTotalElements());
        PageCustom<ReportResponse> pageCustom = this.convertToCustom(page);

        return new ResponseData(
                Boolean.TRUE,
                "Success",
                (Objects.nonNull(pageCustom) && pageCustom.getData() != null)
                        ? pageCustom
                        : Collections.emptyList());
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, HandleReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setIsHandled(true);
        report.setIsViolation(request.getIsViolation());
        report.setAdminNote(request.getAdminNote());
        report.setHandledAt(new Date());
        reportRepository.save(report);

        // Send notification to tenant
        NotificationDto notificationDto = new NotificationDto();

        if (Boolean.TRUE.equals(request.getIsViolation())) {
            Room roomToDelete = report.getRoom();

            // Xóa tất cả các Report liên quan đến Room
            reportRepository.deleteByRoom(roomToDelete);

            // Xóa Room
            roomRepository.delete(roomToDelete);

            notificationDto.setUserId(report.getRoom().getOwner().getId());
            notificationDto.setMessage("Bài đăng của bạn đã bị xóa do vi phạm!");
            notificationDto.setType(NotificationType.BREACH);

            // Gửi thông báo cho chủ bài đăng
            messagingTemplate.convertAndSend("/topic/notifications/" + report.getRoom().getOwner().getId(), notificationDto);
        } else {
            notificationDto.setUserId(report.getReporter().getId());
            notificationDto.setMessage("Báo cáo bài đăng không vi phạm.");
            notificationDto.setType(NotificationType.NON_BREACH);

            // Không vi phạm → Gửi thông báo cho người report
            messagingTemplate.convertAndSend("/topic/notifications/" + report.getReporter().getId(), notificationDto);
        }

        // Save the notification to the database
        notificationService.saveNotification(notificationDto);
    }

    @Override
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return reportMapper.toDto(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        reportRepository.delete(report);
    }

    public PageCustom convertToCustom(final Page<?> pageable) {
        PageCustom pageCustom = new PageCustom();
        PageableCustom pageableCustom = new PageableCustom();
        pageableCustom.setPage(pageable.getNumber() + 1);
        pageableCustom.setLimit(pageable.getSize());
        pageableCustom.setTotalRecords(pageable.getTotalElements());
        pageableCustom.setTotalPage((long) pageable.getTotalPages());
        pageCustom.setPageableCustom(pageableCustom);
        pageCustom.setData(pageable.getContent());
        return pageCustom;
    }

}

