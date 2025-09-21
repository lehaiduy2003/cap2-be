package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.ViewRequestCreateDTO;
import com.c1se_01.roomiego.dto.ViewRequestDTO;
import com.c1se_01.roomiego.dto.ViewRespondDTO;

import java.util.List;

public interface ViewRequestService {
    ViewRequestDTO createRequest(ViewRequestCreateDTO dto);

    List<ViewRequestDTO> getRequestsByOwner();

    ViewRequestDTO respondToRequest(ViewRespondDTO viewRespondDTO);

    ViewRequestDTO cancelRental(ViewRespondDTO viewRespondDTO);
}
