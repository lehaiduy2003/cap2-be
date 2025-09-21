package com.c1se_01.roomiego.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoommateResponseDTO {
    private String gender;
    private String hometown;
    private String city;
    private String district;
    private Integer rateImage;
    private Integer yob;
    private String job;
    private String hobbies;
    private String more;
    private Long userId;
    private String phone;
}
