package com.c1se_01.roomiego.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationMarkerResponse {
    Integer id;
    String address;
    Double longitude;
    Double latitude;
}
