package com.c1se_01.roomiego.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FptAiIdRecognitionResponse {

  @JsonProperty("errorCode")
  private Integer errorCode;

  @JsonProperty("errorMessage")
  private String errorMessage;

  @JsonProperty("data")
  private List<IdCardData> data;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IdCardData {
    @JsonProperty("id")
    private String id;

    @JsonProperty("id_prob")
    private String idProb;

    @JsonProperty("name")
    private String name;

    @JsonProperty("name_prob")
    private String nameProb;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("dob_prob")
    private String dobProb;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("sex_prob")
    private String sexProb;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("nationality_prob")
    private String nationalityProb;

    @JsonProperty("home")
    private String home;

    @JsonProperty("home_prob")
    private String homeProb;

    @JsonProperty("address")
    private String address;

    @JsonProperty("address_prob")
    private String addressProb;

    @JsonProperty("doe")
    private String doe;

    @JsonProperty("doe_prob")
    private String doeProb;

    @JsonProperty("type")
    private String type;

    @JsonProperty("type_prob")
    private String typeProb;

    @JsonProperty("type_new")
    private String typeNew;

    @JsonProperty("address_entities")
    private AddressEntities addressEntities;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AddressEntities {
    @JsonProperty("province")
    private String province;

    @JsonProperty("district")
    private String district;

    @JsonProperty("ward")
    private String ward;

    @JsonProperty("street")
    private String street;
  }
}
