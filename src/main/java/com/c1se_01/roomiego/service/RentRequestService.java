package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.RentRequestCreateRequest;
import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.dto.RentRequestUpdateRequest;

import java.util.List;

public interface RentRequestService {
    RentRequestResponse createRentRequest(RentRequestCreateRequest request);

    List<RentRequestResponse> getRequestsByOwner();

    RentRequestResponse updateRentRequestStatus(Long requestId, RentRequestUpdateRequest updateRequest);

    RentRequestResponse confirmViewing(Long requestId);

    RentRequestResponse confirmFinalize(Long requestId);

    RentRequestResponse cancelRental(Long requestId);
}
