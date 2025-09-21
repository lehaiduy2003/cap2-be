package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.model.ViewRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ViewRequestMapper {
    @Mapping(source = "renter.id", target = "renterId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.owner.id", target = "ownerId")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "adminNote", target = "adminNote")
    @Mapping(source = "createdAt", target = "createdAt")
    ViewRequestDTO toDTO(ViewRequest viewRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "renter", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "adminNote", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    ViewRequest toEntity(ViewRequestCreateDTO dto);
}
