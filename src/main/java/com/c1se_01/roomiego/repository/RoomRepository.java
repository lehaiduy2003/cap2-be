package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByOwnerId(Long ownerId);
}
