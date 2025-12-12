package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.UserDetailDTO;
import com.c1se_01.roomiego.mapper.RoomMapper;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoomMapper roomMapper;

  @Override
  public UserDetailDTO getUserProfile(Long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserDetailDTO userDetailDTO = new UserDetailDTO();
      userDetailDTO.setId(user.getId());
      userDetailDTO.setFullName(user.getFullName());
      userDetailDTO.setPhone(user.getPhone());
      userDetailDTO.setGender(user.getGender() != null ? user.getGender().name() : null);
      userDetailDTO.setDob(user.getDob() != null ? user.getDob().toString() : null);
      userDetailDTO.setBio(user.getBio());
      userDetailDTO.setCreatedAt(user.getCreatedAt());
      userDetailDTO.setAvatarUrl(null);
      userDetailDTO.setJob(null);
      userDetailDTO.setIsVerified(user.getIsVerified());
      userDetailDTO.setVerificationDate(user.getVerificationDate());
      // Map rooms
      List<RoomDTO> roomDTOs = user.getRooms().stream()
          .map(roomMapper::toDTO)
          .collect(Collectors.toList());
      userDetailDTO.setRooms(roomDTOs);
      return userDetailDTO;
    } else {
      throw new RuntimeException("User not found");
    }
  }
}