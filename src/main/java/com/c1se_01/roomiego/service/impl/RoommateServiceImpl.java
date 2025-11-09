package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.AiRecommendationDTO;
import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.RoommateRepository;
import com.c1se_01.roomiego.repository.UserRepository;
import com.c1se_01.roomiego.service.RoommateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoommateServiceImpl implements RoommateService {
    private final RoommateRepository roommateRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.service.url}")
    private String aiServiceUrl;

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

    @Override
    public List<AiRecommendationDTO> getRecommendations(Long userId) {
        try {
            // Call AI service to get recommendations
            String url = aiServiceUrl + "/recommend?user_id=" + userId;
            log.debug("Calling AI service at: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.debug("AI service response: {}", response);
            // Parse JSON response using AI-specific DTO
            List<AiRecommendationDTO> aiRecommendations = objectMapper.readValue(
                response, 
                new TypeReference<List<AiRecommendationDTO>>() {}
            );
            
            // Convert AI DTOs to RoommateResponseDTO
            List<AiRecommendationDTO> recommendations = new ArrayList<>();
            for (AiRecommendationDTO aiRec : aiRecommendations) {
                AiRecommendationDTO dto = new AiRecommendationDTO();
                dto.setUserId(aiRec.getUserId());
                dto.setGender(aiRec.getGender());
                dto.setHometown(aiRec.getHometown());
                dto.setCity(aiRec.getCity());
                dto.setDistrict(aiRec.getDistrict());
                dto.setYob(aiRec.getYob());
                dto.setHobbies(aiRec.getHobbies());
                dto.setJob(aiRec.getJob());
                dto.setMore(aiRec.getMore());
                dto.setRateImage(aiRec.getRateImage());
                
                recommendations.add(dto);
            }
            
            return recommendations;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get recommendations from AI service: " + e.getMessage());
        }
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
