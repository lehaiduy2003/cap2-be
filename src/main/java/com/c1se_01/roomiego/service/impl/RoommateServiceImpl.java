package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.mapper.RoommateMapper;
import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RoommateRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoommateServiceImpl implements RoommateService {
    private final RoommateRepository roommateRepository;
    private final UserRepository userRepository;
    private final RoommateMapper roommateMapper;

    @Override
    public RoommateResponseDTO createRoommate(RoommateDTO dto) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Create roommate entity
        Roommate roommate = new Roommate();
        roommate.setGender(String.valueOf(currentUser.getGender()));
        roommate.setHometown(dto.getHometown());
        roommate.setCity(dto.getCity());
        roommate.setDistrict(dto.getDistrict());
        roommate.setRateImage(dto.getRateImage());
        roommate.setYob(dto.getYob());
        roommate.setJob(dto.getJob());
        roommate.setHobbies(dto.getHobbies());
        roommate.setMore(dto.getMore());
        roommate.setPhone(dto.getPhone());
        roommate.setUser(currentUser);

        // Save and map to response
        Roommate savedRoommate = roommateRepository.save(roommate);
        return mapToResponseDTO(savedRoommate);
    }

    @Override
    public List<RoommateResponseDTO> getAllRoommates() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        
        List<RoommateResponseDTO> roommateList = new ArrayList<>();
        if (Objects.nonNull(currentUser)) {
            List<Roommate> roommates = roommateRepository.findAllByGender(String.valueOf(currentUser.getGender()));
            roommateList = roommates.stream()
                .filter(roommate -> !currentUser.getId().equals(roommate.getUser().getId()))
                .map(this::mapToResponseDTO)
                .toList();
        }
        return roommateList;
    }

    private RoommateResponseDTO mapToResponseDTO(Roommate roommate) {
        RoommateResponseDTO dto = new RoommateResponseDTO();
        dto.setGender(roommate.getGender());
        dto.setHometown(roommate.getHometown());
        dto.setCity(roommate.getCity());
        dto.setDistrict(roommate.getDistrict());
        dto.setRateImage(roommate.getRateImage());
        dto.setYob(roommate.getYob());
        dto.setJob(roommate.getJob());
        dto.setHobbies(roommate.getHobbies());
        dto.setMore(roommate.getMore());
        dto.setPhone(roommate.getPhone());
        dto.setUserId(roommate.getUser().getId());
        return dto;
    }
}
