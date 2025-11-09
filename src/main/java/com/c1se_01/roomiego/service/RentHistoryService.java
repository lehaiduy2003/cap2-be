package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.RentHistoryCreateRequest;
import com.c1se_01.roomiego.dto.RentHistoryResponse;

import java.util.List;

public interface RentHistoryService {
    RentHistoryResponse createRentHistory(RentHistoryCreateRequest request);
    List<RentHistoryResponse> getRentHistoriesByUser(Long userId);
    void confirmReviewed(Long rentHistoryId);
}
