package com.c1se_01.roomiego.dto;


import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationMarkerResponse {
    Long id;
    String address;
    Double longitude;
    Double latitude;
    Optional<RoomDTO> roomData;
    boolean isHasRoomData;
}
