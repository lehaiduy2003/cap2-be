package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ApiResponse;
import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.service.RoommateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roommates")
@RequiredArgsConstructor
public class RoomateController {

    private final RoommateService roommateService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoommateResponseDTO>> createRoommate(@Valid @RequestBody RoommateDTO dto) {
        RoommateResponseDTO responseDTO = roommateService.createRoommate(dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tạo thông tin roommate thành công", responseDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoommateResponseDTO>>> getAllRoommates() {
        List<RoommateResponseDTO> roommates = roommateService.getAllRoommates();
        return ResponseEntity.ok(new ApiResponse<>(200, "Danh sách roommate", roommates));
    }

    @GetMapping("/export-to-file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportRoommatesToFile() throws IOException {
        List<RoommateResponseDTO> roommates = roommateService.getAllRoommates();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(roommates);

        String filePath = "D:/RoomieGO/roommate_finder/data.json";
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            file.delete();
        }

        Files.write(file.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> response = new HashMap<>();
        response.put("filePath", filePath);

        return ResponseEntity.ok(new ApiResponse<>(200, "Xuất dữ liệu thành công", response));
    }
}
