package com.c1se_01.roomiego.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for mapping AI service recommendation response
 * Matches the structure returned by the Python AI service
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecommendationDTO {

  @JsonProperty("user_id")
  private Long userId;

  @JsonProperty("gender")
  private String gender;

  @JsonProperty("hometown")
  private String hometown;

  @JsonProperty("city")
  private String city;

  @JsonProperty("district")
  private String district;

  @JsonProperty("yob")
  private Integer yob;

  @JsonProperty("hobbies")
  private String hobbies;

  @JsonProperty("job")
  private String job;

  @JsonProperty("more")
  private String more;

  @JsonProperty("rate_image")
  private Integer rateImage;
}
