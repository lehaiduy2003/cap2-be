package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RentHistoryResponse;
import com.c1se_01.roomiego.model.RentHistory;
import com.c1se_01.roomiego.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentHistoryMapper {
    @Mapping(source = "rentRequest.room.id", target = "roomId")
    @Mapping(source = "rentRequest.room.title", target = "roomTitle")
    // Compose address from room.addressDetails + district + ward + city
    @Mapping(target = "address", expression = "java(composeAddress(rentHistory))")
    @Mapping(source = "rentRequest.room.description", target = "description")
    @Mapping(source = "rentRequest.room.longitude", target = "longitude")
    @Mapping(source = "rentRequest.room.latitude", target = "latitude")
    RentHistoryResponse toDto(RentHistory rentHistory);

    // default helper used by MapStruct expression to build the address string safely
    default String composeAddress(RentHistory rentHistory) {
        if (rentHistory == null
                || rentHistory.getRentRequest() == null
                || rentHistory.getRentRequest().getRoom() == null) {
            return null;
        }
        Room room = rentHistory.getRentRequest().getRoom();
        return Arrays.asList(
                room.getAddressDetails(),
                room.getDistrict(),
                room.getWard(),
                room.getCity()
        ).stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }
}
