package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.exception.ForbiddenException;
import com.c1se_01.roomiego.exception.NotFoundException;
import com.c1se_01.roomiego.mapper.RoomMapper;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.RoomImage;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RoomImageRepository;
import com.c1se_01.roomiego.repository.RoomRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;  // Tiêm RoomImageRepository vào đây

    private final UserRepository userRepository;

    private final RoomMapper roomMapper;
    @Override
    public RoomDTO createRoom(RoomDTO roomDTO, Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        if (!StringUtils.pathEquals(Role.OWNER.name(), user.getRole().name())) {
            throw new ForbiddenException("User không có quyền tạo phòng");
        }

        roomDTO.setOwnerId(ownerId); // Gán ownerId chính xác

        Room room = roomMapper.toEntity(roomDTO);
        Room savedRoom = roomRepository.save(room);

        if (roomDTO.getImageUrls() != null && !roomDTO.getImageUrls().isEmpty()) {
            List<RoomImage> roomImages = roomDTO.getImageUrls().stream()
                    .map(imageUrl -> new RoomImage(null, savedRoom, imageUrl))
                    .collect(Collectors.toList());
            roomImageRepository.saveAll(roomImages);
            savedRoom.setRoomImages(roomImages);
        }

        return roomMapper.toDTO(savedRoom);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            throw new NotFoundException("Không có phòng nào được tìm thấy");
        }
        return rooms.stream().map(roomMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Phòng không tồn tại"));
        return roomMapper.toDTO(room);
    }

    @Override
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO, Long ownerId) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phòng không tồn tại"));

        if (!existingRoom.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("User không có quyền chỉnh sửa phòng này");
        }

        roomMapper.updateEntityFromDTO(roomDTO, existingRoom);
        Room updatedRoom = roomRepository.save(existingRoom);
        return roomMapper.toDTO(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phòng không tồn tại"));

        roomRepository.delete(room);
    }

    @Override
    public void hideRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setIsRoomAvailable(false);
        roomRepository.save(room);
    }

    @Override
    public List<RoomDTO> getRoomsByOwner(Long ownerId) {
        List<Room> rooms = roomRepository.findByOwnerId(ownerId);
        if (rooms.isEmpty()) {
            throw new NotFoundException("Không có phòng nào được tìm thấy cho owner này");
        }
        return rooms.stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }


}
