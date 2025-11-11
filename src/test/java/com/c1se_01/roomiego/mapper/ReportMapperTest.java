package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.model.Report;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportMapperTest {

  private ReportMapper reportMapper;

  @BeforeEach
  void setUp() {
    reportMapper = Mappers.getMapper(ReportMapper.class);
  }

  // Tests for toDto(Report report)

  @Test
  void testToDto_HappyCase() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setAddressDetails("123 Test Street");

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("John Doe");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(reporter);
    report.setReason("Spam content");
    report.setIsHandled(false);
    report.setIsViolation(null);
    report.setAdminNote(null);
    report.setCreatedAt(new Date());

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertEquals(10L, response.getId());
    assertEquals(1L, response.getRoomId());
    assertEquals("Test Room", response.getRoomTitle());
    assertEquals("123 Test Street", response.getRoomAddress());
    assertEquals("John Doe", response.getReporterName());
    assertEquals("Spam content", response.getReason());
    assertFalse(response.getIsHandled());
    assertNull(response.getIsViolation());
    assertNull(response.getAdminNote());
    assertNotNull(response.getCreatedAt());
  }

  @Test
  void testToDto_NullRoom() {
    // Arrange
    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("John Doe");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(null);
    report.setReporter(reporter);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertNull(response.getRoomId());
    assertNull(response.getRoomTitle());
    assertNull(response.getRoomAddress());
    assertEquals("John Doe", response.getReporterName());
  }

  @Test
  void testToDto_NullReporter() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setAddressDetails("123 Test Street");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(null);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertEquals(1L, response.getRoomId());
    assertEquals("Test Room", response.getRoomTitle());
    assertEquals("123 Test Street", response.getRoomAddress());
    assertNull(response.getReporterName());
  }

  @Test
  void testToDto_NullRoomId() {
    // Arrange
    Room room = new Room();
    room.setId(null);
    room.setTitle("Test Room");
    room.setAddressDetails("123 Test Street");

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("John Doe");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(reporter);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertNull(response.getRoomId());
    assertEquals("Test Room", response.getRoomTitle());
    assertEquals("123 Test Street", response.getRoomAddress());
    assertEquals("John Doe", response.getReporterName());
  }

  @Test
  void testToDto_NullRoomTitle() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle(null);
    room.setAddressDetails("123 Test Street");

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("John Doe");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(reporter);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertEquals(1L, response.getRoomId());
    assertNull(response.getRoomTitle());
    assertEquals("123 Test Street", response.getRoomAddress());
    assertEquals("John Doe", response.getReporterName());
  }

  @Test
  void testToDto_NullRoomAddressDetails() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setAddressDetails(null);

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("John Doe");

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(reporter);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertEquals(1L, response.getRoomId());
    assertEquals("Test Room", response.getRoomTitle());
    assertNull(response.getRoomAddress());
    assertEquals("John Doe", response.getReporterName());
  }

  @Test
  void testToDto_NullReporterFullName() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setAddressDetails("123 Test Street");

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName(null);

    Report report = new Report();
    report.setId(10L);
    report.setRoom(room);
    report.setReporter(reporter);

    // Act
    ReportResponse response = reportMapper.toDto(report);

    // Assert
    assertNotNull(response);
    assertEquals(1L, response.getRoomId());
    assertEquals("Test Room", response.getRoomTitle());
    assertEquals("123 Test Street", response.getRoomAddress());
    assertNull(response.getReporterName());
  }

  @Test
  void testToDto_NullReport() {
    // Act
    ReportResponse response = reportMapper.toDto((Report) null);

    // Assert
    assertNull(response);
  }

  // Tests for toDto(List<Report> reports)

  @Test
  void testToDtoList_HappyCase() {
    // Arrange
    Room room1 = new Room();
    room1.setId(1L);
    room1.setTitle("Room 1");
    room1.setAddressDetails("Address 1");

    User reporter1 = new User();
    reporter1.setId(2L);
    reporter1.setFullName("Reporter 1");

    Report report1 = new Report();
    report1.setId(10L);
    report1.setRoom(room1);
    report1.setReporter(reporter1);

    Room room2 = new Room();
    room2.setId(3L);
    room2.setTitle("Room 2");
    room2.setAddressDetails("Address 2");

    User reporter2 = new User();
    reporter2.setId(4L);
    reporter2.setFullName("Reporter 2");

    Report report2 = new Report();
    report2.setId(20L);
    report2.setRoom(room2);
    report2.setReporter(reporter2);

    List<Report> reports = Arrays.asList(report1, report2);

    // Act
    List<ReportResponse> responses = reportMapper.toDto(reports);

    // Assert
    assertNotNull(responses);
    assertEquals(2, responses.size());

    ReportResponse response1 = responses.get(0);
    assertEquals(10L, response1.getId());
    assertEquals(1L, response1.getRoomId());
    assertEquals("Room 1", response1.getRoomTitle());
    assertEquals("Address 1", response1.getRoomAddress());
    assertEquals("Reporter 1", response1.getReporterName());

    ReportResponse response2 = responses.get(1);
    assertEquals(20L, response2.getId());
    assertEquals(3L, response2.getRoomId());
    assertEquals("Room 2", response2.getRoomTitle());
    assertEquals("Address 2", response2.getRoomAddress());
    assertEquals("Reporter 2", response2.getReporterName());
  }

  @Test
  void testToDtoList_EmptyList() {
    // Arrange
    List<Report> reports = Collections.emptyList();

    // Act
    List<ReportResponse> responses = reportMapper.toDto(reports);

    // Assert
    assertNotNull(responses);
    assertTrue(responses.isEmpty());
  }

  @Test
  void testToDtoList_NullList() {
    // Act
    List<ReportResponse> responses = reportMapper.toDto((List<Report>) null);

    // Assert
    assertNull(responses);
  }

  @Test
  void testToDtoList_WithNullElements() {
    // Arrange
    Room room = new Room();
    room.setId(1L);
    room.setTitle("Test Room");
    room.setAddressDetails("Address");

    User reporter = new User();
    reporter.setId(2L);
    reporter.setFullName("Reporter");

    Report validReport = new Report();
    validReport.setId(10L);
    validReport.setRoom(room);
    validReport.setReporter(reporter);

    List<Report> reports = Arrays.asList(validReport, null);

    // Act
    List<ReportResponse> responses = reportMapper.toDto(reports);

    // Assert
    assertNotNull(responses);
    assertEquals(2, responses.size());
    assertNotNull(responses.get(0));
    assertNull(responses.get(1));
  }
}