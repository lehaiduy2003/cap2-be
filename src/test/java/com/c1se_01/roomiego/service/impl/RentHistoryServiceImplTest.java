package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RentHistoryCreateRequest;
import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.mapper.RentHistoryMapper;
import com.c1se_01.roomiego.model.RentHistory;
import com.c1se_01.roomiego.repository.RentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentHistoryServiceImplTest {

  @Mock
  private RentHistoryRepository rentHistoryRepository;

  @Mock
  private RentHistoryMapper rentHistoryMapper;

  @InjectMocks
  private RentHistoryServiceImpl rentHistoryService;

  private RentHistoryCreateRequest createRequest;
  private RentHistory rentHistory;
  private RentHistoryResponse rentHistoryResponse;

  @BeforeEach
  void setUp() {
    createRequest = new RentHistoryCreateRequest();
    createRequest.setUserId(1L);
    createRequest.setRentRequestId(2L);
    createRequest.setRentDate(LocalDateTime.now());
    createRequest.setReturnDate(LocalDateTime.now().plusDays(30));

    rentHistory = new RentHistory();
    rentHistory.setId(1L);
    rentHistory.setUserId(1L);
    rentHistory.setRentRequestId(2L);
    rentHistory.setRentDate(createRequest.getRentDate());
    rentHistory.setReturnDate(createRequest.getReturnDate());
    rentHistory.setReviewed(false);

    rentHistoryResponse = new RentHistoryResponse();
    rentHistoryResponse.setId(1L);
    rentHistoryResponse.setUserId(1L);
    rentHistoryResponse.setRentRequestId(2L);
    rentHistoryResponse.setRentDate(createRequest.getRentDate());
    rentHistoryResponse.setReturnDate(createRequest.getReturnDate());
    rentHistoryResponse.setReviewed(false);
  }

  @Test
  void createRentHistory_SuccessfulCreation() {
    when(rentHistoryRepository.save(any(RentHistory.class))).thenReturn(rentHistory);
    when(rentHistoryMapper.toDto(rentHistory)).thenReturn(rentHistoryResponse);

    RentHistoryResponse result = rentHistoryService.createRentHistory(createRequest);

    assertNotNull(result);
    assertEquals(rentHistoryResponse.getId(), result.getId());
    assertEquals(rentHistoryResponse.getUserId(), result.getUserId());
    assertEquals(rentHistoryResponse.getRentRequestId(), result.getRentRequestId());
    verify(rentHistoryRepository, times(1)).save(any(RentHistory.class));
    verify(rentHistoryMapper, times(1)).toDto(rentHistory);
  }

  @Test
  void getRentHistoriesByUser_WithResults() {
    List<RentHistory> rentHistories = List.of(rentHistory);

    when(rentHistoryRepository.findByUserIdWithRoom(1L)).thenReturn(rentHistories);
    when(rentHistoryMapper.toDto(rentHistory)).thenReturn(rentHistoryResponse);

    List<RentHistoryResponse> result = rentHistoryService.getRentHistoriesByUser(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(rentHistoryResponse, result.get(0));
    verify(rentHistoryRepository, times(1)).findByUserIdWithRoom(1L);
    verify(rentHistoryMapper, times(1)).toDto(rentHistory);
  }

  @Test
  void getRentHistoriesByUser_EmptyList() {
    when(rentHistoryRepository.findByUserIdWithRoom(1L)).thenReturn(Collections.emptyList());

    List<RentHistoryResponse> result = rentHistoryService.getRentHistoriesByUser(1L);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(rentHistoryRepository, times(1)).findByUserIdWithRoom(1L);
    verify(rentHistoryMapper, never()).toDto(any(RentHistory.class));
  }

  @Test
  void confirmReviewed_SuccessfulConfirmation() {
    when(rentHistoryRepository.findById(1L)).thenReturn(Optional.of(rentHistory));

    assertDoesNotThrow(() -> rentHistoryService.confirmReviewed(1L));

    assertTrue(rentHistory.getReviewed());
    verify(rentHistoryRepository, times(1)).findById(1L);
    verify(rentHistoryRepository, times(1)).save(rentHistory);
  }

  @Test
  void confirmReviewed_RentHistoryNotFound() {
    when(rentHistoryRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> rentHistoryService.confirmReviewed(1L));

    assertEquals("Rent history not found", exception.getMessage());
    verify(rentHistoryRepository, times(1)).findById(1L);
    verify(rentHistoryRepository, never()).save(any(RentHistory.class));
  }
}