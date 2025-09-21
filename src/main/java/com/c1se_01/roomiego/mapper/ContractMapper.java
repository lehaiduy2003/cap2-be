package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.ContractResponse;
import com.c1se_01.roomiego.model.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "owner.id", target = "ownerId")
    ContractResponse toDto(Contract contract);
}
