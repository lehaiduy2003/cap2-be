package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.enums.Gender;
import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoommateMapperTest {

  private RoommateMapper roommateMapper;

  @BeforeEach
  void setUp() {
    roommateMapper = Mappers.getMapper(RoommateMapper.class);
  }

  // Tests for toEntity

  @Test
  void testToEntity_HappyCase() {
    // Arrange
    RoommateDTO dto = new RoommateDTO();
    dto.setHometown("Hometown");
    dto.setCity("City");
    dto.setDistrict("District");
    dto.setRateImage(5);
    dto.setYob(1990);
    dto.setJob("Job");
    dto.setHobbies("Hobbies");
    dto.setMore("More");
    dto.setPhone("Phone");
    dto.setUserId(1L);

    User user = new User();
    user.setId(1L);
    user.setGender(Gender.MALE);

    // Act
    Roommate roommate = roommateMapper.toEntity(dto, user);

    // Assert
    assertNotNull(roommate);
    assertEquals("Hometown", roommate.getHometown());
    assertEquals("City", roommate.getCity());
    assertEquals("District", roommate.getDistrict());
    assertEquals(5, roommate.getRateImage());
    assertEquals(1990, roommate.getYob());
    assertEquals("Job", roommate.getJob());
    assertEquals("Hobbies", roommate.getHobbies());
    assertEquals("More", roommate.getMore());
    assertEquals("Phone", roommate.getPhone());
    assertEquals("MALE", roommate.getGender());
    assertEquals(user, roommate.getUser());
  }

  @Test
  void testToEntity_WithNullFields() {
    // Arrange
    RoommateDTO dto = new RoommateDTO();
    // All fields null except userId
    dto.setUserId(1L);

    User user = new User();
    user.setId(1L);
    user.setGender(Gender.FEMALE);

    // Act
    Roommate roommate = roommateMapper.toEntity(dto, user);

    // Assert
    assertNotNull(roommate);
    assertNull(roommate.getHometown());
    assertNull(roommate.getCity());
    assertNull(roommate.getDistrict());
    assertNull(roommate.getRateImage());
    assertNull(roommate.getYob());
    assertNull(roommate.getJob());
    assertNull(roommate.getHobbies());
    assertNull(roommate.getMore());
    assertNull(roommate.getPhone());
    assertEquals("FEMALE", roommate.getGender());
    assertEquals(user, roommate.getUser());
  }

  @Test
  void testToEntity_WithNullDto() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setGender(Gender.MALE);

    // Act
    Roommate roommate = roommateMapper.toEntity(null, user);

    // Assert
    assertNull(roommate);
  }

  @Test
  void testToEntity_WithNullUser() {
    // Arrange
    RoommateDTO dto = new RoommateDTO();
    dto.setUserId(1L);

    // Act & Assert
    assertThrows(NullPointerException.class, () -> roommateMapper.toEntity(dto, null));
  }

  // Tests for toResponseDTO

  @Test
  void testToResponseDTO_HappyCase() {
    // Arrange
    User user = new User();
    user.setId(1L);

    Roommate roommate = new Roommate();
    roommate.setId(1L);
    roommate.setGender("MALE");
    roommate.setHometown("Hometown");
    roommate.setCity("City");
    roommate.setDistrict("District");
    roommate.setRateImage(5);
    roommate.setYob(1990);
    roommate.setPhone("Phone");
    roommate.setJob("Job");
    roommate.setHobbies("Hobbies");
    roommate.setMore("More");
    roommate.setUser(user);

    // Act
    RoommateResponseDTO responseDTO = roommateMapper.toResponseDTO(roommate);

    // Assert
    assertNotNull(responseDTO);
    assertEquals("MALE", responseDTO.getGender());
    assertEquals("Hometown", responseDTO.getHometown());
    assertEquals("City", responseDTO.getCity());
    assertEquals("District", responseDTO.getDistrict());
    assertEquals(5, responseDTO.getRateImage());
    assertEquals(1990, responseDTO.getYob());
    assertEquals("Phone", responseDTO.getPhone());
    assertEquals("Job", responseDTO.getJob());
    assertEquals("Hobbies", responseDTO.getHobbies());
    assertEquals("More", responseDTO.getMore());
    assertEquals(1L, responseDTO.getUserId());
  }

  @Test
  void testToResponseDTO_WithNullFields() {
    // Arrange
    User user = new User();
    user.setId(2L);

    Roommate roommate = new Roommate();
    roommate.setUser(user);
    // All other fields null

    // Act
    RoommateResponseDTO responseDTO = roommateMapper.toResponseDTO(roommate);

    // Assert
    assertNotNull(responseDTO);
    assertNull(responseDTO.getGender());
    assertNull(responseDTO.getHometown());
    assertNull(responseDTO.getCity());
    assertNull(responseDTO.getDistrict());
    assertNull(responseDTO.getRateImage());
    assertNull(responseDTO.getYob());
    assertNull(responseDTO.getPhone());
    assertNull(responseDTO.getJob());
    assertNull(responseDTO.getHobbies());
    assertNull(responseDTO.getMore());
    assertEquals(2L, responseDTO.getUserId());
  }

  @Test
  void testToResponseDTO_WithNullRoommate() {
    // Act
    RoommateResponseDTO responseDTO = roommateMapper.toResponseDTO(null);

    // Assert
    assertNull(responseDTO);
  }

  @Test
  void testToResponseDTO_WithNullUser() {
    // Arrange
    Roommate roommate = new Roommate();
    // user is null

    // Act
    RoommateResponseDTO responseDTO = roommateMapper.toResponseDTO(roommate);

    // Assert
    assertNotNull(responseDTO);
    assertNull(responseDTO.getUserId());
  }

  // Tests for toResponseDTOs

  @Test
  void testToResponseDTOs_HappyCase() {
    // Arrange
    User user1 = new User();
    user1.setId(1L);

    Roommate roommate1 = new Roommate();
    roommate1.setId(1L);
    roommate1.setGender("MALE");
    roommate1.setHometown("Hometown1");
    roommate1.setUser(user1);

    User user2 = new User();
    user2.setId(2L);

    Roommate roommate2 = new Roommate();
    roommate2.setId(2L);
    roommate2.setGender("FEMALE");
    roommate2.setHometown("Hometown2");
    roommate2.setUser(user2);

    List<Roommate> roommates = Arrays.asList(roommate1, roommate2);

    // Act
    List<RoommateResponseDTO> responseDTOs = roommateMapper.toResponseDTOs(roommates);

    // Assert
    assertNotNull(responseDTOs);
    assertEquals(2, responseDTOs.size());
    assertEquals("MALE", responseDTOs.get(0).getGender());
    assertEquals("Hometown1", responseDTOs.get(0).getHometown());
    assertEquals(1L, responseDTOs.get(0).getUserId());
    assertEquals("FEMALE", responseDTOs.get(1).getGender());
    assertEquals("Hometown2", responseDTOs.get(1).getHometown());
    assertEquals(2L, responseDTOs.get(1).getUserId());
  }

  @Test
  void testToResponseDTOs_WithEmptyList() {
    // Arrange
    List<Roommate> roommates = Collections.emptyList();

    // Act
    List<RoommateResponseDTO> responseDTOs = roommateMapper.toResponseDTOs(roommates);

    // Assert
    assertNotNull(responseDTOs);
    assertTrue(responseDTOs.isEmpty());
  }

  @Test
  void testToResponseDTOs_WithNullList() {
    // Act
    List<RoommateResponseDTO> responseDTOs = roommateMapper.toResponseDTOs(null);

    // Assert
    assertNull(responseDTOs);
  }
}