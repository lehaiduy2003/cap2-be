package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RentHistoryCreateRequest;
import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.mapper.RentHistoryMapper;
import com.c1se_01.roomiego.model.RentHistory;
import com.c1se_01.roomiego.repository.RentHistoryRepository;
import com.c1se_01.roomiego.service.RentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentHistoryServiceImpl implements RentHistoryService {
    private final RentHistoryRepository rentHistoryRepository;
    private final RentHistoryMapper rentHistoryMapper;

    @Override
    public RentHistoryResponse createRentHistory(RentHistoryCreateRequest request) {
        RentHistory rentHistory = new RentHistory();
        rentHistory.setUserId(request.getUserId());
        rentHistory.setRentRequestId(request.getRentRequestId());
        rentHistory.setRentDate(request.getRentDate());
        rentHistory.setReturnDate(request.getReturnDate());

        RentHistory saved = rentHistoryRepository.save(rentHistory);
        return rentHistoryMapper.toDto(saved);
    }

    @Override
    public List<RentHistoryResponse> getRentHistoriesByUser(Long userId) {
        List<RentHistory> rentHistories = rentHistoryRepository.findByUserIdWithRoom(userId);
        return rentHistories.stream()
                .map(rentHistoryMapper::toDto).toList();
    }

    @Override
    public void confirmReviewed(Long rentHistoryId) {
        RentHistory rentHistory = rentHistoryRepository.findById(rentHistoryId)
            .orElseThrow(() -> new RuntimeException("Rent history not found"));
        rentHistory.setReviewed(true);
        rentHistoryRepository.save(rentHistory);
    }
}
