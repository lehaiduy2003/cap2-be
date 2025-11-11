package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.RoomService;
import com.c1se_01.roomiego.service.impl.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoomControllerTest {

  @Mock
  private RoomService roomService;

  @Mock
  private FileStorageService fileStorageService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private User mockUser;
  private RoomDTO mockRoomDTO;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new RoomController(roomService, fileStorageService)).build();
    objectMapper = new ObjectMapper();

    // Setup mock user
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setFullName("Test User");

    // Setup mock authentication
    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Setup mock RoomDTO
    mockRoomDTO = new RoomDTO();
    mockRoomDTO.setId(1L);
    mockRoomDTO.setTitle("Test Room");
    mockRoomDTO.setDescription("Test Description");
    mockRoomDTO.setPrice(new BigDecimal("1000"));
    mockRoomDTO.setLocation("Test Location");
    mockRoomDTO.setRoomSize(50.0f);
    mockRoomDTO.setNumBedrooms(2);
    mockRoomDTO.setNumBathrooms(1);
    mockRoomDTO.setIsRoomAvailable(true);
    mockRoomDTO.setCity("Test City");
    mockRoomDTO.setDistrict("Test District");
    mockRoomDTO.setWard("Test Ward");
    mockRoomDTO.setStreet("Test Street");
    mockRoomDTO.setAddressDetails("Test Details");
    mockRoomDTO.setImageUrls(Arrays.asList("image1.jpg", "image2.jpg"));
  }

  @Test
  public void testCreateRoomJson_HappyCase() throws Exception {
    when(roomService.createRoom(any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(post("/api/rooms")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(mockRoomDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo phòng thành công"))
        .andExpect(jsonPath("$.data.id").value(1L));
  }

  @Test
  public void testCreateRoomWithImage_HappyCase() throws Exception {
    MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "content1".getBytes());
    MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", "image/jpeg", "content2".getBytes());

    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("stored_image1.jpg")
        .thenReturn("stored_image2.jpg");
    when(roomService.createRoom(any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(multipart("/api/rooms")
        .file(image1)
        .file(image2)
        .param("title", "Test Room")
        .param("description", "Test Description")
        .param("price", "1000")
        .param("location", "Test Location")
        .param("roomSize", "50.0")
        .param("numBedrooms", "2")
        .param("numBathrooms", "1")
        .param("isRoomAvailable", "true")
        .param("city", "Test City")
        .param("district", "Test District")
        .param("ward", "Test Ward")
        .param("street", "Test Street")
        .param("addressDetails", "Test Details"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo phòng thành công"))
        .andExpect(jsonPath("$.data.id").value(1L));
  }

  @Test
  public void testCreateRoomWithImage_EmptyNumericFields() throws Exception {
    when(roomService.createRoom(any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(multipart("/api/rooms")
        .param("title", "Test Room")
        .param("description", "Test Description")
        .param("price", "")
        .param("location", "Test Location")
        .param("roomSize", "")
        .param("numBedrooms", "")
        .param("numBathrooms", "")
        .param("isRoomAvailable", "true")
        .param("city", "Test City")
        .param("district", "Test District")
        .param("ward", "Test Ward")
        .param("street", "Test Street")
        .param("addressDetails", "Test Details"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo phòng thành công"));
  }

  @Test
  public void testCreateRoomWithImage_NoImages() throws Exception {
    when(roomService.createRoom(any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(multipart("/api/rooms")
        .param("title", "Test Room")
        .param("description", "Test Description")
        .param("price", "1000")
        .param("location", "Test Location")
        .param("roomSize", "50.0")
        .param("numBedrooms", "2")
        .param("numBathrooms", "1")
        .param("isRoomAvailable", "true")
        .param("city", "Test City")
        .param("district", "Test District")
        .param("ward", "Test Ward")
        .param("street", "Test Street")
        .param("addressDetails", "Test Details"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo phòng thành công"));
  }

  @Test
  public void testCreateRoomWithImage_EmptyImagesArray() throws Exception {
    MockMultipartFile emptyImage = new MockMultipartFile("images", "", "image/jpeg", new byte[0]);

    when(roomService.createRoom(any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(multipart("/api/rooms")
        .file(emptyImage)
        .param("title", "Test Room")
        .param("description", "Test Description")
        .param("price", "1000")
        .param("location", "Test Location")
        .param("roomSize", "50.0")
        .param("numBedrooms", "2")
        .param("numBathrooms", "1")
        .param("isRoomAvailable", "true")
        .param("city", "Test City")
        .param("district", "Test District")
        .param("ward", "Test Ward")
        .param("street", "Test Street")
        .param("addressDetails", "Test Details"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Tạo phòng thành công"));
  }

  @Test
  public void testGetAllRooms_HappyCase() throws Exception {
    List<RoomDTO> rooms = Arrays.asList(mockRoomDTO);
    when(roomService.getAllRooms(any(FilterParam.class))).thenReturn(rooms);

    mockMvc.perform(get("/api/rooms"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Danh sách phòng"))
        .andExpect(jsonPath("$.data[0].id").value(1L));
  }

  @Test
  public void testGetAllRooms_WithFilters() throws Exception {
    List<RoomDTO> rooms = Arrays.asList(mockRoomDTO);
    when(roomService.getAllRooms(any(FilterParam.class))).thenReturn(rooms);

    mockMvc.perform(get("/api/rooms")
        .param("search", "test")
        .param("page", "1")
        .param("size", "5")
        .param("sort", "title")
        .param("filter", "price:>500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Danh sách phòng"));
  }

  @Test
  public void testGetRoomById_HappyCase() throws Exception {
    when(roomService.getRoomById(1L)).thenReturn(mockRoomDTO);

    mockMvc.perform(get("/api/rooms/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Chi tiết phòng"))
        .andExpect(jsonPath("$.data.id").value(1L));
  }

  @Test
  public void testUpdateRoom_HappyCase() throws Exception {
    when(roomService.updateRoom(eq(1L), any(RoomDTO.class), eq(1L))).thenReturn(mockRoomDTO);

    mockMvc.perform(put("/api/rooms/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(mockRoomDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Cập nhật phòng thành công"))
        .andExpect(jsonPath("$.data.id").value(1L));
  }

  @Test
  public void testDeleteRoom_HappyCase() throws Exception {
    mockMvc.perform(delete("/api/rooms/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Xóa phòng thành công"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  public void testGetRoomsByOwnerId_HappyCase() throws Exception {
    List<RoomDTO> rooms = Arrays.asList(mockRoomDTO);
    when(roomService.getRoomsByOwner(2L)).thenReturn(rooms);

    mockMvc.perform(get("/api/rooms/owner/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Danh sách phòng của owner"))
        .andExpect(jsonPath("$.data[0].id").value(1L));
  }

  @Test
  public void testGetMyRooms_HappyCase() throws Exception {
    List<RoomDTO> rooms = Arrays.asList(mockRoomDTO);
    when(roomService.getRoomsByOwner(1L)).thenReturn(rooms);

    mockMvc.perform(get("/api/rooms/owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("Danh sách phòng của owner"))
        .andExpect(jsonPath("$.data[0].id").value(1L));
  }

  @Test
  public void testHideRoom_HappyCase() throws Exception {
    mockMvc.perform(post("/api/rooms/1/hide"))
        .andExpect(status().isOk())
        .andExpect(content().string("Room hidden successfully"));
  }
}