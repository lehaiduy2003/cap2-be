package com.c1se_01.roomiego.dto;

import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchRequest {
    private String address;
    @Nullable
    private Double longitude;
    @Nullable
    private Double latitude;
}
