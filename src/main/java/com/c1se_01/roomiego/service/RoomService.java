package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.RoomDTO;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    RoomDTO createRoom(RoomDTO roomDTO, Long ownerId);
    RoomDTO updateRoom(Long id, RoomDTO roomDTO, Long ownerId);

    List<RoomDTO> getAllRooms();
    List<RoomDTO> getRoomsByOwner(Long ownerId);
    RoomDTO getRoomById(Long id);
    void deleteRoom(Long id);

    void hideRoom(Long roomId);
}
