package com.c1se_01.roomiego.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocationMarkerRequest {
    String address;
    Integer id;
}
