package com.c1se_01.roomiego.mapper;

import com.c1se_01.roomiego.dto.ReportResponse;
import com.c1se_01.roomiego.model.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.title", target = "roomTitle")
    @Mapping(source = "room.addressDetails", target = "roomAddress")
    @Mapping(source = "reporter.fullName", target = "reporterName")
    ReportResponse toDto(Report report);

    List<ReportResponse> toDto(List<Report> reports);
}
