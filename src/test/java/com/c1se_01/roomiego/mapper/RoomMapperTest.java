package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.RoomImage;
import com.c1se_01.roomiego.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomMapperTest {

  private RoomMapper roomMapper;

  @BeforeEach
  void setUp() {
    roomMapper = Mappers.getMapper(RoomMapper.class);
  }

  // Tests for toDTO

  @Test
  void testToDTO_HappyCase() {
    // Arrange
    User owner = new User();
    owner.setId(1L);
    owner.setFullName("John Doe");

    RoomImage image1 = new RoomImage();
    image1.setImageUrl("url1");
    RoomImage image2 = new RoomImage();
    image2.setImageUrl("url2");
    List<RoomImage> roomImages = Arrays.asList(image1, image2);

    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setDescription("Description");
    room.setPrice(BigDecimal.valueOf(1000));
    room.setLocation("Location");
    room.setLatitude(10.0);
    room.setLongitude(20.0);
    room.setRoomSize(50.0f);
    room.setNumBedrooms(2);
    room.setNumBathrooms(1);
    room.setAvailableFrom(new Date());
    room.setIsRoomAvailable(true);
    room.setCity("City");
    room.setDistrict("District");
    room.setWard("Ward");
    room.setStreet("Street");
    room.setAddressDetails("Details");
    room.setOwner(owner);
    room.setRoomImages(roomImages);

    // Act
    RoomDTO dto = roomMapper.toDTO(room);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Test Room", dto.getTitle());
    assertEquals("Description", dto.getDescription());
    assertEquals(BigDecimal.valueOf(1000), dto.getPrice());
    assertEquals("Location", dto.getLocation());
    assertEquals(10.0, dto.getLatitude());
    assertEquals(20.0, dto.getLongitude());
    assertEquals(50.0f, dto.getRoomSize());
    assertEquals(2, dto.getNumBedrooms());
    assertEquals(1, dto.getNumBathrooms());
    assertNotNull(dto.getAvailableFrom());
    assertTrue(dto.getIsRoomAvailable());
    assertEquals("City", dto.getCity());
    assertEquals("District", dto.getDistrict());
    assertEquals("Ward", dto.getWard());
    assertEquals("Street", dto.getStreet());
    assertEquals("Details", dto.getAddressDetails());
    assertEquals(1L, dto.getOwnerId());
    assertEquals("John Doe", dto.getOwnerName());
    assertNotNull(dto.getImageUrls());
    assertEquals(2, dto.getImageUrls().size());
    assertTrue(dto.getImageUrls().contains("url1"));
    assertTrue(dto.getImageUrls().contains("url2"));
  }

  @Test
  void testToDTO_NullOwner() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setRoomImages(Collections.emptyList());

    // Act
    RoomDTO dto = roomMapper.toDTO(room);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertNull(dto.getOwnerId());
    assertNull(dto.getOwnerName());
    assertNotNull(dto.getImageUrls());
    assertTrue(dto.getImageUrls().isEmpty());
  }

  @Test
  void testToDTO_NullRoomImages() {
    // Arrange
    User owner = new User();
    owner.setId(1L);
    owner.setFullName("John Doe");

    Room room = new Room();
    room.setId(1L);
    room.setOwner(owner);
    room.setRoomImages(null);

    // Act
    RoomDTO dto = roomMapper.toDTO(room);

    // Assert
    assertNotNull(dto);
    assertEquals(1L, dto.getOwnerId());
    assertEquals("John Doe", dto.getOwnerName());
    assertNull(dto.getImageUrls());
  }

  @Test
  void testToDTO_EmptyRoomImages() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setRoomImages(Collections.emptyList());

    // Act
    RoomDTO dto = roomMapper.toDTO(room);

    // Assert
    assertNotNull(dto);
    assertNotNull(dto.getImageUrls());
    assertTrue(dto.getImageUrls().isEmpty());
  }

  // Tests for toEntity

  @Test
  void testToEntity_HappyCase() {
    // Arrange
    RoomDTO dto = new RoomDTO();
    dto.setId(1L);
    dto.setTitle("Test Room");
    dto.setDescription("Description");
    dto.setPrice(BigDecimal.valueOf(1000));
    dto.setLocation("Location");
    dto.setLatitude(10.0);
    dto.setLongitude(20.0);
    dto.setRoomSize(50.0f);
    dto.setNumBedrooms(2);
    dto.setNumBathrooms(1);
    dto.setAvailableFrom(new Date());
    dto.setIsRoomAvailable(true);
    dto.setCity("City");
    dto.setDistrict("District");
    dto.setWard("Ward");
    dto.setStreet("Street");
    dto.setAddressDetails("Details");
    dto.setOwnerId(1L);
    dto.setOwnerName("John Doe");
    dto.setImageUrls(Arrays.asList("url1", "url2"));

    // Act
    Room room = roomMapper.toEntity(dto);

    // Assert
    assertNotNull(room);
    assertEquals(1L, room.getId());
    assertEquals("Test Room", room.getTitle());
    assertEquals("Description", room.getDescription());
    assertEquals(BigDecimal.valueOf(1000), room.getPrice());
    assertEquals("Location", room.getLocation());
    assertEquals(10.0, room.getLatitude());
    assertEquals(20.0, room.getLongitude());
    assertEquals(50.0f, room.getRoomSize());
    assertEquals(2, room.getNumBedrooms());
    assertEquals(1, room.getNumBathrooms());
    assertNotNull(room.getAvailableFrom());
    assertTrue(room.getIsRoomAvailable());
    assertEquals("City", room.getCity());
    assertEquals("District", room.getDistrict());
    assertEquals("Ward", room.getWard());
    assertEquals("Street", room.getStreet());
    assertEquals("Details", room.getAddressDetails());
    assertNotNull(room.getOwner());
    assertEquals(1L, room.getOwner().getId());
    // Note: roomImages are not mapped in toEntity, only in toDTO
  }

  @Test
  void testToEntity_NullFields() {
    // Arrange
    RoomDTO dto = new RoomDTO();
    dto.setId(1L);
    // Leave other fields null

    // Act
    Room room = roomMapper.toEntity(dto);

    // Assert
    assertNotNull(room);
    assertEquals(1L, room.getId());
    assertNull(room.getTitle());
    assertNotNull(room.getOwner()); // MapStruct creates User instance
    assertNull(room.getOwner().getId()); // But id is null
  }

  // Tests for toDTOList

  @Test
  void testToDTOList_HappyCase() {
    // Arrange
    User owner = new User();
    owner.setId(1L);
    owner.setFullName("John Doe");

    Room room1 = new Room();
    room1.setId(1L);
    room1.setTitle("Room 1");
    room1.setOwner(owner);
    room1.setRoomImages(Collections.emptyList());

    Room room2 = new Room();
    room2.setId(2L);
    room2.setTitle("Room 2");
    room2.setOwner(owner);
    room2.setRoomImages(Collections.emptyList());

    List<Room> rooms = Arrays.asList(room1, room2);

    // Act
    List<RoomDTO> dtos = roomMapper.toDTOList(rooms);

    // Assert
    assertNotNull(dtos);
    assertEquals(2, dtos.size());
    assertEquals("Room 1", dtos.get(0).getTitle());
    assertEquals("Room 2", dtos.get(1).getTitle());
  }

  @Test
  void testToDTOList_EmptyList() {
    // Arrange
    List<Room> rooms = Collections.emptyList();

    // Act
    List<RoomDTO> dtos = roomMapper.toDTOList(rooms);

    // Assert
    assertNotNull(dtos);
    assertTrue(dtos.isEmpty());
  }

  @Test
  void testToDTOList_NullList() {
    // Arrange
    List<Room> rooms = null;

    // Act
    List<RoomDTO> dtos = roomMapper.toDTOList(rooms);

    // Assert
    assertNull(dtos);
  }

  // Tests for updateEntityFromDTO

  @Test
  void testUpdateEntityFromDTO_HappyCase() {
    // Arrange
    Room entity = new Room();
    entity.setId(1L);
    entity.setTitle("Old Title");

    RoomDTO dto = new RoomDTO();
    dto.setTitle("New Title");
    dto.setDescription("New Description");
    dto.setPrice(BigDecimal.valueOf(2000));
    dto.setLocation("New Location");
    dto.setLatitude(30.0);
    dto.setLongitude(40.0);
    dto.setRoomSize(60.0f);
    dto.setNumBedrooms(3);
    dto.setNumBathrooms(2);
    dto.setAvailableFrom(new Date());
    dto.setIsRoomAvailable(false);
    dto.setCity("New City");
    dto.setDistrict("New District");
    dto.setWard("New Ward");
    dto.setStreet("New Street");
    dto.setAddressDetails("New Details");

    // Act
    roomMapper.updateEntityFromDTO(dto, entity);

    // Assert
    assertEquals("New Title", entity.getTitle());
    assertEquals("New Description", entity.getDescription());
    assertEquals(BigDecimal.valueOf(2000), entity.getPrice());
    assertEquals("New Location", entity.getLocation());
    assertEquals(30.0, entity.getLatitude());
    assertEquals(40.0, entity.getLongitude());
    assertEquals(60.0f, entity.getRoomSize());
    assertEquals(3, entity.getNumBedrooms());
    assertEquals(2, entity.getNumBathrooms());
    assertNotNull(entity.getAvailableFrom());
    assertFalse(entity.getIsRoomAvailable());
    assertEquals("New City", entity.getCity());
    assertEquals("New District", entity.getDistrict());
    assertEquals("New Ward", entity.getWard());
    assertEquals("New Street", entity.getStreet());
    assertEquals("New Details", entity.getAddressDetails());
  }

  @Test
  void testUpdateEntityFromDTO_PartialUpdate() {
    // Arrange
    Room entity = new Room();
    entity.setId(1L);
    entity.setTitle("Old Title");
    entity.setDescription("Old Description");

    RoomDTO dto = new RoomDTO();
    dto.setTitle("New Title");
    // Leave description null

    // Act
    roomMapper.updateEntityFromDTO(dto, entity);

    // Assert
    assertEquals("New Title", entity.getTitle());
    assertEquals("Old Description", entity.getDescription()); // Should not change
  }

  @Test
  void testUpdateEntityFromDTO_AllNull() {
    // Arrange
    Room entity = new Room();
    entity.setId(1L);
    entity.setTitle("Old Title");
    entity.setDescription("Old Description");

    RoomDTO dto = new RoomDTO();
    // All fields null

    // Act
    roomMapper.updateEntityFromDTO(dto, entity);

    // Assert
    assertEquals("Old Title", entity.getTitle()); // Should not change
    assertEquals("Old Description", entity.getDescription());
  }

  @Test
  void testUpdateEntityFromDTO_NullDTO() {
    // Arrange
    Room entity = new Room();
    entity.setId(1L);
    entity.setTitle("Old Title");

    RoomDTO dto = null;

    // Act & Assert
    assertThrows(NullPointerException.class, () -> roomMapper.updateEntityFromDTO(dto, entity));
    // Since Optional.ofNullable(dto.getTitle()) will throw NPE if dto is null
  }
}