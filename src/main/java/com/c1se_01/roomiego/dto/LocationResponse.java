package com.c1se_01.roomiego.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private String status;
    private String message;
    private LocationData location;
    private List<NearbyPlace> nearbyPlaces;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String formattedAddress;
        private double latitude;
        private double longitude;
        private String placeId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlace {
        private String name;
        private String address;
        private double latitude;
        private double longitude;
        private String placeId;
        private double rating;
        private String type;
        private double distanceInMeters;
    }
}
