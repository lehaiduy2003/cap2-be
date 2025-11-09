package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.RentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentHistoryRepository extends JpaRepository<RentHistory, Long> {
    @Query("select rh from RentHistory rh join fetch rh.rentRequest rr join fetch rr.room where rh.userId = :userId")
    List<RentHistory> findByUserIdWithRoom(@Param("userId") Long userId);
}


