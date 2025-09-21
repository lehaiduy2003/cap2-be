package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.RoommateDTO;
import com.c1se_01.roomiego.dto.RoommateResponseDTO;
import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoommateMapper {
    Roommate toEntity(RoommateDTO dto, @Context User user);

    @Mapping(source = "user.id", target = "userId")
    RoommateResponseDTO toResponseDTO(Roommate roommate);

    List<RoommateResponseDTO> toResponseDTOs(List<Roommate> roommates);

    @ObjectFactory
    default Roommate createRoommate(RoommateDTO dto, @Context User user) {
        Roommate roommate = new Roommate();
        roommate.setGender(String.valueOf(user.getGender()));
        roommate.setHometown(dto.getHometown());
        roommate.setCity(dto.getCity());
        roommate.setDistrict(dto.getDistrict());
        roommate.setRateImage(dto.getRateImage());
        roommate.setYob(dto.getYob());
        roommate.setJob(dto.getJob());
        roommate.setHobbies(dto.getHobbies());
        roommate.setMore(dto.getMore());
        roommate.setUser(user);
        return roommate;
    }
}
