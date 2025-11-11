package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.enums.ViewRequestStatus;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.model.ViewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ViewRequestMapperTest {

  private ViewRequestMapper viewRequestMapper;

  @BeforeEach
  void setUp() {
    viewRequestMapper = Mappers.getMapper(ViewRequestMapper.class);
  }

  // Tests for toDTO

  @Test
  void testToDTO_HappyCase() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("I want to view this room");
    viewRequest.setStatus(ViewRequestStatus.PENDING);
    viewRequest.setAdminNote("Admin note");
    viewRequest.setCreatedAt(new Date());
    viewRequest.setOwnerId(3L);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertEquals(10L, dto.getId());
    assertEquals(1L, dto.getRoomId());
    assertEquals(2L, dto.getRenterId());
    assertEquals(3L, dto.getOwnerId());
    assertEquals("I want to view this room", dto.getMessage());
    assertEquals(ViewRequestStatus.PENDING, dto.getStatus());
    assertEquals("Admin note", dto.getAdminNote());
    assertNotNull(dto.getCreatedAt());
  }

  @Test
  void testToDTO_NullRenter() {
    // Arrange
    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(null);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(ViewRequestStatus.ACCEPTED);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getRoomId());
    assertNull(dto.getRenterId());
    assertEquals(3L, dto.getOwnerId());
    assertEquals("Message", dto.getMessage());
    assertEquals(ViewRequestStatus.ACCEPTED, dto.getStatus());
  }

  @Test
  void testToDTO_NullRoom() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(null);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(ViewRequestStatus.REJECTED);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getRoomId());
    assertEquals(2L, dto.getRenterId());
    assertNull(dto.getOwnerId());
    assertEquals("Message", dto.getMessage());
    assertEquals(ViewRequestStatus.REJECTED, dto.getStatus());
  }

  @Test
  void testToDTO_NullRoomOwner() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(null);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(ViewRequestStatus.PENDING);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getRoomId());
    assertEquals(2L, dto.getRenterId());
    assertNull(dto.getOwnerId());
    assertEquals("Message", dto.getMessage());
    assertEquals(ViewRequestStatus.PENDING, dto.getStatus());
  }

  @Test
  void testToDTO_NullMessage() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage(null);
    viewRequest.setStatus(ViewRequestStatus.PENDING);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getRoomId());
    assertEquals(2L, dto.getRenterId());
    assertEquals(3L, dto.getOwnerId());
    assertNull(dto.getMessage());
    assertEquals(ViewRequestStatus.PENDING, dto.getStatus());
  }

  @Test
  void testToDTO_EmptyMessage() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("");
    viewRequest.setStatus(ViewRequestStatus.PENDING);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertEquals("", dto.getMessage());
  }

  @Test
  void testToDTO_NullStatus() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(null);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getStatus());
  }

  @Test
  void testToDTO_NullAdminNote() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(ViewRequestStatus.PENDING);
    viewRequest.setAdminNote(null);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getAdminNote());
  }

  @Test
  void testToDTO_NullCreatedAt() {
    // Arrange
    User renter = new User();
    renter.setId(2L);

    User owner = new User();
    owner.setId(3L);

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);

    ViewRequest viewRequest = new ViewRequest();
    viewRequest.setId(10L);
    viewRequest.setRoom(room);
    viewRequest.setRenter(renter);
    viewRequest.setMessage("Message");
    viewRequest.setStatus(ViewRequestStatus.PENDING);
    viewRequest.setCreatedAt(null);

    // Act
    ViewRequestDTO dto = viewRequestMapper.toDTO(viewRequest);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getCreatedAt());
  }

  // Tests for toEntity

  @Test
  void testToEntity_HappyCase() {
    // Arrange
    ViewRequestCreateDTO dto = new ViewRequestCreateDTO();
    dto.setRoomId(1L);
    dto.setMessage("I want to view this room");

    // Act
    ViewRequest entity = viewRequestMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertNull(entity.getId()); // ignored
    assertNull(entity.getRoom()); // ignored
    assertNull(entity.getRenter()); // ignored
    assertEquals("I want to view this room", entity.getMessage());
    assertEquals(ViewRequestStatus.PENDING, entity.getStatus()); // default value
    assertNull(entity.getAdminNote()); // ignored
    assertNotNull(entity.getCreatedAt()); // default value
    assertNull(entity.getOwnerId()); // ignored
  }

  @Test
  void testToEntity_NullMessage() {
    // Arrange
    ViewRequestCreateDTO dto = new ViewRequestCreateDTO();
    dto.setRoomId(1L);
    dto.setMessage(null);

    // Act
    ViewRequest entity = viewRequestMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertNull(entity.getMessage());
  }

  @Test
  void testToEntity_EmptyMessage() {
    // Arrange
    ViewRequestCreateDTO dto = new ViewRequestCreateDTO();
    dto.setRoomId(1L);
    dto.setMessage("");

    // Act
    ViewRequest entity = viewRequestMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals("", entity.getMessage());
  }

  @Test
  void testToEntity_NullRoomId() {
    // Arrange
    ViewRequestCreateDTO dto = new ViewRequestCreateDTO();
    dto.setRoomId(null);
    dto.setMessage("Message");

    // Act
    ViewRequest entity = viewRequestMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals("Message", entity.getMessage());
    // roomId is not mapped to entity
  }
}