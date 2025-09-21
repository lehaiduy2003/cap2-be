package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.RentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentRequestRepository extends JpaRepository<RentRequest, Long> {
    List<RentRequest> findByRoomOwnerId(Long ownerId);
}
