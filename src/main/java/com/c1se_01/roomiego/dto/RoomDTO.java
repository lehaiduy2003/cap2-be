package com.c1se_01.roomiego.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String location;
    private Double latitude;
    private Double longitude;
    private Float roomSize;
    private Integer numBedrooms;
    private Integer numBathrooms;
    private Date availableFrom;
    private Boolean isRoomAvailable;
    private Long ownerId;
    private String city;
    private String district;
    private String ward;
    private String street;
    private String addressDetails;
    private String ownerName;
    private List<String> imageUrls;


}
