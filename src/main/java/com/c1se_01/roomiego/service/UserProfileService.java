package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.UserDetailDTO;

public interface UserProfileService {
  UserDetailDTO getUserProfile(Long userId);
}