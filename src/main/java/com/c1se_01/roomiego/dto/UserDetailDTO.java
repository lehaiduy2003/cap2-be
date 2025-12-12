package com.c1se_01.roomiego.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UserDetailDTO {
  private Long id;
  private String fullName;
  private String phone;
  private String gender;
  private String dob;
  private String bio;
  private LocalDateTime createdAt;
  private String avatarUrl;
  private String job;
  private Boolean isVerified;
  private LocalDateTime verificationDate;
  private List<RoomDTO> rooms;
}
