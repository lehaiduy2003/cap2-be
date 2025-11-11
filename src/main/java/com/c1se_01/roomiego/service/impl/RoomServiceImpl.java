package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.common.FilterParam;
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
import com.c1se_01.roomiego.service.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository; // Tiêm RoomImageRepository vào đây

    private final UserRepository userRepository;
    private final GoogleMapsService googleMapsService;

    private final RoomMapper roomMapper;

    @Override
    public RoomDTO createRoom(RoomDTO roomDTO, Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        if (!StringUtils.pathEquals(Role.OWNER.name(), user.getRole().name())) {
            throw new ForbiddenException("User không có quyền tạo phòng");
        }

        roomDTO.setOwnerId(ownerId); // Gán ownerId chính xác

        if (roomDTO.getLatitude() == null || roomDTO.getLongitude() == null) {
            String fullAddress = buildFullAddress(roomDTO);
            if (fullAddress != null) {
                var locationResponse = googleMapsService.geocodeAddress(fullAddress);
                if (locationResponse != null) {
                    roomDTO.setLatitude(locationResponse.getLatitude());
                    roomDTO.setLongitude(locationResponse.getLongitude());
                } else {
                    throw new NotFoundException("Không thể lấy tọa độ cho địa chỉ đã cho");
                }

            }
        }

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
    public List<RoomDTO> getAllRooms(FilterParam filterParam) {
        log.info(filterParam.toString());
        Specification<Room> spec = RoomSpecification.buildSpecification(filterParam);
        log.info(spec.toString());
        List<Room> rooms = roomRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
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
            throw new NotFoundException("Không có phòng nào được tìm thay cho owner này");
        }
        return rooms.stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }

    private String buildFullAddress(RoomDTO roomDTO) {
        List<String> addressParts = new java.util.ArrayList<>();

        if (roomDTO.getAddressDetails() != null && !roomDTO.getAddressDetails().trim().isEmpty()) {
            addressParts.add(roomDTO.getAddressDetails().trim());
        }
        if (roomDTO.getStreet() != null && !roomDTO.getStreet().trim().isEmpty()) {
            addressParts.add(roomDTO.getStreet().trim());
        }
        if (roomDTO.getWard() != null && !roomDTO.getWard().trim().isEmpty()) {
            addressParts.add(roomDTO.getWard().trim());
        }
        if (roomDTO.getDistrict() != null && !roomDTO.getDistrict().trim().isEmpty()) {
            addressParts.add(roomDTO.getDistrict().trim());
        }
        if (roomDTO.getCity() != null && !roomDTO.getCity().trim().isEmpty()) {
            addressParts.add(roomDTO.getCity().trim());
        }

        return addressParts.isEmpty() ? null : String.join(", ", addressParts);
    }
}
