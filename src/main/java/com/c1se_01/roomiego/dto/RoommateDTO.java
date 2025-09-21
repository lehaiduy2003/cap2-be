package com.c1se_01.roomiego.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoommateDTO {
    private String hometown;

    private String city;
    private String district;
    private Integer rateImage;
    private Integer yob;
    private String job;
    private String hobbies;
    private String more;
    private String phone;
    @NotNull(message = "userId không được để trống")
    private Long userId;// để ánh xạ với entity User
}
