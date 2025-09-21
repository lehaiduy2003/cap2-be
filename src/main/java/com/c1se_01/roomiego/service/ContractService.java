package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.ContractCreateRequest;
import com.c1se_01.roomiego.dto.ContractResponse;

import java.util.List;

public interface ContractService {
    ContractResponse createContract(ContractCreateRequest request);

    ContractResponse getContractById(Long contractId);
    
    List<ContractResponse> getAllContracts();

    void deleteContract(Long contractId);
}
