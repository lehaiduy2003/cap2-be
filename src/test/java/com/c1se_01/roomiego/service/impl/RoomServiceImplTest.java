package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.LocationResponse;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RoomServiceImplTest {

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private RoomImageRepository roomImageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private GoogleMapsService googleMapsService;

  @Mock
  private RoomMapper roomMapper;

  @InjectMocks
  private RoomServiceImpl roomService;

  @Test
  void createRoom_HappyPath_WithLatLngAndImages() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    roomDTO.setLatitude(10.0);
    roomDTO.setLongitude(20.0);
    roomDTO.setImageUrls(Arrays.asList("url1", "url2"));

    User user = new User();
    user.setId(ownerId);
    user.setRole(Role.OWNER);

    Room room = new Room();
    Room savedRoom = new Room();
    savedRoom.setId(1L);

    List<RoomImage> roomImages = Arrays.asList(new RoomImage(1L, savedRoom, "url1"),
        new RoomImage(2L, savedRoom, "url2"));
    RoomDTO expectedDTO = new RoomDTO();

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
    when(roomMapper.toEntity(roomDTO)).thenReturn(room);
    when(roomRepository.save(room)).thenReturn(savedRoom);
    when(roomImageRepository.saveAll(anyList())).thenReturn(roomImages);
    when(roomMapper.toDTO(savedRoom)).thenReturn(expectedDTO);

    // When
    RoomDTO result = roomService.createRoom(roomDTO, ownerId);

    // Then
    assertEquals(expectedDTO, result);
    verify(userRepository).findById(ownerId);
    verify(roomMapper).toEntity(roomDTO);
    verify(roomRepository).save(room);
    verify(roomImageRepository).saveAll(anyList());
    verify(roomMapper).toDTO(savedRoom);
    verify(googleMapsService, never()).geocodeAddress(anyString());
  }

  @Test
  void createRoom_HappyPath_WithGeocodingAndNoImages() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    roomDTO.setAddressDetails("123");
    roomDTO.setStreet("Main St");
    roomDTO.setWard("Ward 1");
    roomDTO.setDistrict("District 1");
    roomDTO.setCity("City");
    roomDTO.setImageUrls(Collections.emptyList());

    User user = new User();
    user.setId(ownerId);
    user.setRole(Role.OWNER);

    Room room = new Room();
    Room savedRoom = new Room();
    savedRoom.setId(1L);

    LocationResponse.LocationData locationData = new LocationResponse.LocationData("formatted", 10.0, 20.0, "placeId");
    RoomDTO expectedDTO = new RoomDTO();

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
    when(googleMapsService.geocodeAddress("123, Main St, Ward 1, District 1, City")).thenReturn(locationData);
    when(roomMapper.toEntity(roomDTO)).thenReturn(room);
    when(roomRepository.save(room)).thenReturn(savedRoom);
    when(roomMapper.toDTO(savedRoom)).thenReturn(expectedDTO);

    // When
    RoomDTO result = roomService.createRoom(roomDTO, ownerId);

    // Then
    assertEquals(expectedDTO, result);
    assertEquals(10.0, roomDTO.getLatitude());
    assertEquals(20.0, roomDTO.getLongitude());
    verify(userRepository).findById(ownerId);
    verify(googleMapsService).geocodeAddress(anyString());
    verify(roomRepository).save(room);
    verify(roomImageRepository, never()).saveAll(anyList());
  }

  @Test
  void createRoom_UserNotFound() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();

    when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.createRoom(roomDTO, ownerId));
    verify(userRepository).findById(ownerId);
    verifyNoMoreInteractions(roomRepository, roomImageRepository, googleMapsService, roomMapper);
  }

  @Test
  void createRoom_UserNotOwner() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();

    User user = new User();
    user.setId(ownerId);
    user.setRole(Role.RENTER);

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));

    // When & Then
    assertThrows(ForbiddenException.class, () -> roomService.createRoom(roomDTO, ownerId));
    verify(userRepository).findById(ownerId);
    verifyNoMoreInteractions(roomRepository, roomImageRepository, googleMapsService, roomMapper);
  }

  @Test
  void createRoom_GeocodingFails() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    roomDTO.setCity("City");

    User user = new User();
    user.setId(ownerId);
    user.setRole(Role.OWNER);

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
    when(googleMapsService.geocodeAddress("City")).thenReturn(null);

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.createRoom(roomDTO, ownerId));
    verify(userRepository).findById(ownerId);
    verify(googleMapsService).geocodeAddress(anyString());
    verifyNoMoreInteractions(roomRepository, roomImageRepository, roomMapper);
  }

  @Test
  void createRoom_NoAddressForGeocoding() {
    // Given
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    // No address parts set

    User user = new User();
    user.setId(ownerId);
    user.setRole(Role.OWNER);

    Room room = new Room();
    Room savedRoom = new Room();
    RoomDTO expectedDTO = new RoomDTO();

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
    when(roomMapper.toEntity(roomDTO)).thenReturn(room);
    when(roomRepository.save(room)).thenReturn(savedRoom);
    when(roomMapper.toDTO(savedRoom)).thenReturn(expectedDTO);

    // When
    RoomDTO result = roomService.createRoom(roomDTO, ownerId);

    // Then
    assertEquals(expectedDTO, result);
    verify(userRepository).findById(ownerId);
    verify(googleMapsService, never()).geocodeAddress(anyString());
    verify(roomRepository).save(room);
  }

  @Test
  void getAllRooms_HappyPath() {
    // Given
    FilterParam filterParam = new FilterParam();
    List<Room> rooms = Arrays.asList(new Room(), new Room());
    List<RoomDTO> expectedDTOs = Arrays.asList(new RoomDTO(), new RoomDTO());

    when(roomRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(rooms);
    when(roomMapper.toDTO(any(Room.class))).thenReturn(new RoomDTO());

    // When
    List<RoomDTO> result = roomService.getAllRooms(filterParam);

    // Then
    assertEquals(expectedDTOs.size(), result.size());
    verify(roomRepository).findAll(any(Specification.class), any(Sort.class));
  }

  @Test
  void getAllRooms_NoRoomsFound() {
    // Given
    FilterParam filterParam = new FilterParam();
    when(roomRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(Collections.emptyList());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.getAllRooms(filterParam));
    verify(roomRepository).findAll(any(Specification.class), any(Sort.class));
  }

  @Test
  void getRoomById_HappyPath() {
    // Given
    Long roomId = 1L;
    Room room = new Room();
    RoomDTO expectedDTO = new RoomDTO();

    when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
    when(roomMapper.toDTO(room)).thenReturn(expectedDTO);

    // When
    RoomDTO result = roomService.getRoomById(roomId);

    // Then
    assertEquals(expectedDTO, result);
    verify(roomRepository).findById(roomId);
    verify(roomMapper).toDTO(room);
  }

  @Test
  void getRoomById_RoomNotFound() {
    // Given
    Long roomId = 1L;
    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.getRoomById(roomId));
    verify(roomRepository).findById(roomId);
    verifyNoMoreInteractions(roomMapper);
  }

  @Test
  void updateRoom_HappyPath() {
    // Given
    Long roomId = 1L;
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    Room existingRoom = new Room();
    User owner = new User();
    owner.setId(ownerId);
    existingRoom.setOwner(owner);
    Room updatedRoom = new Room();
    RoomDTO expectedDTO = new RoomDTO();

    when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
    when(roomRepository.save(existingRoom)).thenReturn(updatedRoom);
    when(roomMapper.toDTO(updatedRoom)).thenReturn(expectedDTO);

    // When
    RoomDTO result = roomService.updateRoom(roomId, roomDTO, ownerId);

    // Then
    assertEquals(expectedDTO, result);
    verify(roomRepository).findById(roomId);
    verify(roomMapper).updateEntityFromDTO(roomDTO, existingRoom);
    verify(roomRepository).save(existingRoom);
    verify(roomMapper).toDTO(updatedRoom);
  }

  @Test
  void updateRoom_RoomNotFound() {
    // Given
    Long roomId = 1L;
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();

    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.updateRoom(roomId, roomDTO, ownerId));
    verify(roomRepository).findById(roomId);
    verifyNoMoreInteractions(roomMapper, roomRepository);
  }

  @Test
  void updateRoom_OwnerNotMatch() {
    // Given
    Long roomId = 1L;
    Long ownerId = 1L;
    RoomDTO roomDTO = new RoomDTO();
    Room existingRoom = new Room();
    User owner = new User();
    owner.setId(2L); // Different owner
    existingRoom.setOwner(owner);

    when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));

    // When & Then
    assertThrows(ForbiddenException.class, () -> roomService.updateRoom(roomId, roomDTO, ownerId));
    verify(roomRepository).findById(roomId);
    verifyNoMoreInteractions(roomMapper, roomRepository);
  }

  @Test
  void deleteRoom_HappyPath() {
    // Given
    Long roomId = 1L;
    Room room = new Room();

    when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

    // When
    roomService.deleteRoom(roomId);

    // Then
    verify(roomRepository).findById(roomId);
    verify(roomRepository).delete(room);
  }

  @Test
  void deleteRoom_RoomNotFound() {
    // Given
    Long roomId = 1L;
    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.deleteRoom(roomId));
    verify(roomRepository).findById(roomId);
    verify(roomRepository, never()).delete(any(Room.class));
  }

  @Test
  void hideRoom_HappyPath() {
    // Given
    Long roomId = 1L;
    Room room = new Room();

    when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

    // When
    roomService.hideRoom(roomId);

    // Then
    assertFalse(room.getIsRoomAvailable());
    verify(roomRepository).findById(roomId);
    verify(roomRepository).save(room);
  }

  @Test
  void hideRoom_RoomNotFound() {
    // Given
    Long roomId = 1L;
    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(RuntimeException.class, () -> roomService.hideRoom(roomId));
    verify(roomRepository).findById(roomId);
    verify(roomRepository, never()).save(any(Room.class));
  }

  @Test
  void getRoomsByOwner_HappyPath() {
    // Given
    Long ownerId = 1L;
    List<Room> rooms = Arrays.asList(new Room(), new Room());
    List<RoomDTO> expectedDTOs = Arrays.asList(new RoomDTO(), new RoomDTO());

    when(roomRepository.findByOwnerId(ownerId)).thenReturn(rooms);
    when(roomMapper.toDTO(any(Room.class))).thenReturn(new RoomDTO());

    // When
    List<RoomDTO> result = roomService.getRoomsByOwner(ownerId);

    // Then
    assertEquals(expectedDTOs.size(), result.size());
    verify(roomRepository).findByOwnerId(ownerId);
  }

  @Test
  void getRoomsByOwner_NoRoomsFound() {
    // Given
    Long ownerId = 1L;
    when(roomRepository.findByOwnerId(ownerId)).thenReturn(Collections.emptyList());

    // When & Then
    assertThrows(NotFoundException.class, () -> roomService.getRoomsByOwner(ownerId));
    verify(roomRepository).findByOwnerId(ownerId);
    verifyNoMoreInteractions(roomMapper);
  }
}