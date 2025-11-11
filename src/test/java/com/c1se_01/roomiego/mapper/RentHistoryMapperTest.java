package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.model.RentHistory;
import com.c1se_01.roomiego.model.RentRequest;
import com.c1se_01.roomiego.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RentHistoryMapperTest {

  private RentHistoryMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(RentHistoryMapper.class);
  }

  @Test
  void toDto_ShouldMapAllFields_WhenAllDataPresent() {
    // Given
    RentHistory rentHistory = createRentHistoryWithAllData();

    // When
    RentHistoryResponse response = mapper.toDto(rentHistory);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getUserId()).isEqualTo(1L);
    assertThat(response.getRentRequestId()).isEqualTo(2L);
    assertThat(response.getRoomId()).isEqualTo(10L);
    assertThat(response.getRoomTitle()).isEqualTo("Beautiful Room");
    assertThat(response.getAddress()).isEqualTo("123 Main St, Downtown, Central Ward, New York");
    assertThat(response.getDescription()).isEqualTo("A nice room");
    assertThat(response.getLongitude()).isEqualTo(40.7128);
    assertThat(response.getLatitude()).isEqualTo(-74.0060);
    assertThat(response.getRentDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
    assertThat(response.getReturnDate()).isEqualTo(LocalDateTime.of(2023, 2, 1, 10, 0));
    assertThat(response.getReviewed()).isTrue();
  }

  @Test
  void toDto_ShouldReturnNull_WhenRentHistoryIsNull() {
    // When
    RentHistoryResponse response = mapper.toDto(null);

    // Then
    assertThat(response).isNull();
  }

  @Test
  void toDto_ShouldHandleNullRentRequest() {
    // Given
    RentHistory rentHistory = new RentHistory();
    rentHistory.setId(1L);
    rentHistory.setUserId(1L);
    rentHistory.setRentRequestId(2L);
    rentHistory.setRentRequest(null); // null rentRequest

    // When
    RentHistoryResponse response = mapper.toDto(rentHistory);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getUserId()).isEqualTo(1L);
    assertThat(response.getRentRequestId()).isEqualTo(2L);
    assertThat(response.getRoomId()).isNull();
    assertThat(response.getRoomTitle()).isNull();
    assertThat(response.getAddress()).isNull();
    assertThat(response.getDescription()).isNull();
    assertThat(response.getLongitude()).isNull();
    assertThat(response.getLatitude()).isNull();
  }

  @Test
  void toDto_ShouldHandleNullRoom() {
    // Given
    RentHistory rentHistory = new RentHistory();
    rentHistory.setId(1L);
    rentHistory.setUserId(1L);
    rentHistory.setRentRequestId(2L);

    RentRequest rentRequest = new RentRequest();
    rentRequest.setId(2L);
    rentRequest.setRoom(null); // null room

    rentHistory.setRentRequest(rentRequest);

    // When
    RentHistoryResponse response = mapper.toDto(rentHistory);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getRoomId()).isNull();
    assertThat(response.getRoomTitle()).isNull();
    assertThat(response.getAddress()).isNull();
    assertThat(response.getDescription()).isNull();
    assertThat(response.getLongitude()).isNull();
    assertThat(response.getLatitude()).isNull();
  }

  @Test
  void composeAddress_ShouldReturnNull_WhenRentHistoryIsNull() {
    // When
    String address = mapper.composeAddress(null);

    // Then
    assertThat(address).isNull();
  }

  @Test
  void composeAddress_ShouldReturnNull_WhenRentRequestIsNull() {
    // Given
    RentHistory rentHistory = new RentHistory();
    rentHistory.setRentRequest(null);

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isNull();
  }

  @Test
  void composeAddress_ShouldReturnNull_WhenRoomIsNull() {
    // Given
    RentHistory rentHistory = new RentHistory();
    RentRequest rentRequest = new RentRequest();
    rentRequest.setRoom(null);
    rentHistory.setRentRequest(rentRequest);

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isNull();
  }

  @Test
  void composeAddress_ShouldReturnJoinedAddress_WhenAllAddressPartsPresent() {
    // Given
    RentHistory rentHistory = createRentHistoryWithAllData();

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isEqualTo("123 Main St, Downtown, Central Ward, New York");
  }

  @Test
  void composeAddress_ShouldFilterNullAndEmptyParts() {
    // Given
    RentHistory rentHistory = new RentHistory();
    RentRequest rentRequest = new RentRequest();
    Room room = new Room();
    room.setAddressDetails("123 Main St");
    room.setDistrict(null); // null
    room.setWard(""); // empty
    room.setCity("New York");
    rentRequest.setRoom(room);
    rentHistory.setRentRequest(rentRequest);

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isEqualTo("123 Main St, New York");
  }

  @Test
  void composeAddress_ShouldReturnEmptyString_WhenAllAddressPartsAreNullOrEmpty() {
    // Given
    RentHistory rentHistory = new RentHistory();
    RentRequest rentRequest = new RentRequest();
    Room room = new Room();
    room.setAddressDetails(null);
    room.setDistrict(""); // empty
    room.setWard(null);
    room.setCity("   "); // whitespace only
    rentRequest.setRoom(room);
    rentHistory.setRentRequest(rentRequest);

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isEmpty();
  }

  @Test
  void composeAddress_ShouldHandleWhitespaceOnlyStrings() {
    // Given
    RentHistory rentHistory = new RentHistory();
    RentRequest rentRequest = new RentRequest();
    Room room = new Room();
    room.setAddressDetails("123 Main St");
    room.setDistrict("   "); // whitespace only
    room.setWard("Central Ward");
    room.setCity("New York");
    rentRequest.setRoom(room);
    rentHistory.setRentRequest(rentRequest);

    // When
    String address = mapper.composeAddress(rentHistory);

    // Then
    assertThat(address).isEqualTo("123 Main St, Central Ward, New York");
  }

  private RentHistory createRentHistoryWithAllData() {
    RentHistory rentHistory = new RentHistory();
    rentHistory.setId(1L);
    rentHistory.setUserId(1L);
    rentHistory.setRentRequestId(2L);
    rentHistory.setRentDate(LocalDateTime.of(2023, 1, 1, 10, 0));
    rentHistory.setReturnDate(LocalDateTime.of(2023, 2, 1, 10, 0));
    rentHistory.setReviewed(true);

    RentRequest rentRequest = new RentRequest();
    rentRequest.setId(2L);

    Room room = new Room();
    room.setId(10L);
    room.setTitle("Beautiful Room");
    room.setDescription("A nice room");
    room.setLongitude(40.7128);
    room.setLatitude(-74.0060);
    room.setAddressDetails("123 Main St");
    room.setDistrict("Downtown");
    room.setWard("Central Ward");
    room.setCity("New York");

    rentRequest.setRoom(room);
    rentHistory.setRentRequest(rentRequest);

    return rentHistory;
  }
}