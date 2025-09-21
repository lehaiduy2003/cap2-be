package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RentRequestResponse;
import com.c1se_01.roomiego.model.RentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentRequestMapper {
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "tenantFinalize", target = "tenantFinalize")
    @Mapping(source = "ownerFinalize", target = "ownerFinalize")
    RentRequestResponse toDto(RentRequest rentRequest);
}