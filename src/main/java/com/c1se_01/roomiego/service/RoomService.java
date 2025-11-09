package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.common.FilterParam;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(RoomDTO roomDTO, Long ownerId);
    RoomDTO updateRoom(Long id, RoomDTO roomDTO, Long ownerId);

    List<RoomDTO> getAllRooms(FilterParam filter);
    List<RoomDTO> getRoomsByOwner(Long ownerId);
    RoomDTO getRoomById(Long id);
    void deleteRoom(Long id);

    void hideRoom(Long roomId);
}
