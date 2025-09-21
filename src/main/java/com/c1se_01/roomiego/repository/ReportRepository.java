package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Report;
import com.c1se_01.roomiego.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Page<Report>> findAllByIsHandled(Boolean isHandled, Pageable pageable);

    void deleteByRoom(Room room);
}
