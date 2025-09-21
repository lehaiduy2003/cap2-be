package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.ViewRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewRequestRepository extends JpaRepository<ViewRequest, Long> {
    @Query("SELECT vr FROM ViewRequest vr " +
           "LEFT JOIN FETCH vr.room r " +
           "LEFT JOIN FETCH vr.renter " +
           "WHERE r.owner.id = :ownerId")
    List<ViewRequest> findByOwnerId(@Param("ownerId") Long ownerId);
}
