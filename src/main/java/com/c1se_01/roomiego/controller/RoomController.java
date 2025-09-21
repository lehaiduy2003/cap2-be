package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ApiResponse;
import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.RoomService;
import com.c1se_01.roomiego.service.impl.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final FileStorageService fileStorageService;

    // For JSON requests (no file upload)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RoomDTO>> createRoomJson(@RequestBody RoomDTO roomDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long ownerId = user.getId();

        RoomDTO createdRoom = roomService.createRoom(roomDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, "Tạo phòng thành công", createdRoom));
    }

    // For multipart form data requests (with file upload)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RoomDTO>> createRoomWithImage(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "roomSize", required = false) String roomSize,
            @RequestParam(value = "numBedrooms", required = false) String numBedrooms,
            @RequestParam(value = "numBathrooms", required = false) String numBathrooms,
            @RequestParam(value = "availableFrom", required = false) String availableFrom,
            @RequestParam(value = "isRoomAvailable", defaultValue = "true") Boolean isRoomAvailable,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "street", required = false) String street,
            @RequestParam(value = "addressDetails", required = false) String addressDetails,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long ownerId = user.getId();

        // Convert string values to appropriate types
        Float roomSizeValue = roomSize != null && !roomSize.isEmpty() ? Float.parseFloat(roomSize) : null;
        Integer bedroomsValue = numBedrooms != null && !numBedrooms.isEmpty() ? Integer.parseInt(numBedrooms) : null;
        Integer bathroomsValue = numBathrooms != null && !numBathrooms.isEmpty() ? Integer.parseInt(numBathrooms) : null;
        java.math.BigDecimal priceValue = price != null && !price.isEmpty() ? new java.math.BigDecimal(price) : null;

        // Create RoomDTO object
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setTitle(title);
        roomDTO.setDescription(description);
        roomDTO.setPrice(priceValue);
        roomDTO.setLocation(location);
        roomDTO.setRoomSize(roomSizeValue);
        roomDTO.setNumBedrooms(bedroomsValue);
        roomDTO.setNumBathrooms(bathroomsValue);
        // Parse date if needed
        // roomDTO.setAvailableFrom(parseDate(availableFrom));
        roomDTO.setIsRoomAvailable(isRoomAvailable);
        roomDTO.setCity(city);
        roomDTO.setDistrict(district);
        roomDTO.setWard(ward);
        roomDTO.setStreet(street);
        roomDTO.setAddressDetails(addressDetails);

        // Handle multiple image uploads
        List<String> imageUrls = new ArrayList<>();
        if (images != null && images.length > 0) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    String imageUrl = fileStorageService.storeFile(image);
                    imageUrls.add(imageUrl);
                }
            }
        }
        roomDTO.setImageUrls(imageUrls);

        // Create room
        RoomDTO createdRoom = roomService.createRoom(roomDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, "Tạo phòng thành công", createdRoom));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(new ApiResponse<>(200, "Danh sách phòng", rooms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable Long id) {
        RoomDTO roomDTO = roomService.getRoomById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Chi tiết phòng", roomDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> updateRoom(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Long ownerId = user.getId();
        RoomDTO updatedRoom = roomService.updateRoom(id, roomDTO, ownerId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật phòng thành công", updatedRoom));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa phòng thành công", null));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getRoomsByOwnerId(@PathVariable Long ownerId) {
        List<RoomDTO> rooms = roomService.getRoomsByOwner(ownerId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Danh sách phòng của owner", rooms));
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getMyRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Long ownerId = user.getId();
        List<RoomDTO> rooms = roomService.getRoomsByOwner(ownerId);

        return ResponseEntity.ok(new ApiResponse<>(200, "Danh sách phòng của owner", rooms));
    }

    @PostMapping("/{roomId}/hide")
    public String hideRoom(@PathVariable Long roomId) {
        roomService.hideRoom(roomId);
        return "Room hidden successfully";
    }

}

