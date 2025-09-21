package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;

import java.util.List;


public interface RoommateService {
    RoommateResponseDTO createRoommate(RoommateDTO dto);

    List<RoommateResponseDTO> getAllRoommates();
}
