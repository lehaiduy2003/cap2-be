package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.ApiResponse;
import com.c1se_01.roomiego.dto.ContractCreateRequest;
import com.c1se_01.roomiego.dto.ContractResponse;
import com.c1se_01.roomiego.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getAllContracts() {
        List<ContractResponse> contracts = contractService.getAllContracts();
        return ResponseEntity.ok(new ApiResponse<>(200, "Danh sách hợp đồng", contracts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(@RequestBody ContractCreateRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tạo hợp đồng thành công", response));
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long contractId) {
        ContractResponse response = contractService.getContractById(contractId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Chi tiết hợp đồng", response));
    }

    @PostMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<ContractResponse>> createContractForRoom(
            @PathVariable Long roomId,
            @RequestBody ContractCreateRequest request) {
        request.setRoomId(roomId);
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tạo hợp đồng thành công", response));
    }

    @DeleteMapping("/{contractId}")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long contractId) {
        contractService.deleteContract(contractId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa hợp đồng thành công", null));
    }
}
