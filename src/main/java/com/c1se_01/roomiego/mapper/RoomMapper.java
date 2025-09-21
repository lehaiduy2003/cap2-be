package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.model.RoomImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "roomImages", target = "imageUrls", qualifiedByName = "mapRoomImagesToUrls")
    @Mapping(source = "owner.fullName", target = "ownerName")

    RoomDTO toDTO(Room room);

    @Mapping(source = "ownerId", target = "owner.id")
    Room toEntity(RoomDTO roomDTO);

    List<RoomDTO> toDTOList(List<Room> rooms);

    // ✅ Custom mapping từ List<RoomImage> -> List<String>
    @Named("mapRoomImagesToUrls")
    default List<String> mapRoomImagesToUrls(List<RoomImage> roomImages) {
        if (roomImages == null) return null;
        return roomImages.stream()
                .map(RoomImage::getImageUrl)
                .collect(Collectors.toList());
    }

    default void updateEntityFromDTO(RoomDTO dto, Room entity) {
        Optional.ofNullable(dto.getTitle()).ifPresent(entity::setTitle);
        Optional.ofNullable(dto.getDescription()).ifPresent(entity::setDescription);
        Optional.ofNullable(dto.getPrice()).ifPresent(entity::setPrice);
        Optional.ofNullable(dto.getLocation()).ifPresent(entity::setLocation);
        Optional.ofNullable(dto.getLatitude()).ifPresent(entity::setLatitude);
        Optional.ofNullable(dto.getLongitude()).ifPresent(entity::setLongitude);
        Optional.ofNullable(dto.getRoomSize()).ifPresent(entity::setRoomSize);
        Optional.ofNullable(dto.getNumBedrooms()).ifPresent(entity::setNumBedrooms);
        Optional.ofNullable(dto.getNumBathrooms()).ifPresent(entity::setNumBathrooms);
        Optional.ofNullable(dto.getAvailableFrom()).ifPresent(entity::setAvailableFrom);
        Optional.ofNullable(dto.getIsRoomAvailable()).ifPresent(entity::setIsRoomAvailable);
        Optional.ofNullable(dto.getCity()).ifPresent(entity::setCity);
        Optional.ofNullable(dto.getDistrict()).ifPresent(entity::setDistrict);
        Optional.ofNullable(dto.getWard()).ifPresent(entity::setWard);
        Optional.ofNullable(dto.getStreet()).ifPresent(entity::setStreet);
        Optional.ofNullable(dto.getAddressDetails()).ifPresent(entity::setAddressDetails);
    }
}
