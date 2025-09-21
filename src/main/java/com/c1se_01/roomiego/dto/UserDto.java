package com.c1se_01.roomiego.dto;

import com.c1se_01.roomiego.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private Long id;
    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String fullName;
    private String role;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String dob;
    private String bio;
    private LocalDateTime createdAt;
    private User user;

    private List<User> usersList;

}