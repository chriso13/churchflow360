package com.churchflow360.attendanceservice.mapper;

import com.churchflow360.attendanceservice.dto.request.CreateServiceEventRequest;
import com.churchflow360.attendanceservice.dto.response.AttendanceRecordResponse;
import com.churchflow360.attendanceservice.dto.response.ServiceEventResponse;
import com.churchflow360.attendanceservice.model.AttendanceRecord;
import com.churchflow360.attendanceservice.model.ServiceEvent;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttendanceMapper {

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "qrToken",           ignore = true)
    @Mapping(target = "qrExpiresAt",       ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "createdAt",         ignore = true)
    @Mapping(target = "updatedAt",         ignore = true)
    ServiceEvent toEntity(CreateServiceEventRequest request);

    @Mapping(target = "qrValid",          expression = "java(event.isQrValid())")
    @Mapping(target = "totalAttendance",  expression = "java(event.getTotalAttendance())")
    ServiceEventResponse toResponse(ServiceEvent event);

    @Mapping(target = "serviceEventId",    source = "serviceEvent.id")
    @Mapping(target = "serviceEventTitle", source = "serviceEvent.title")
    AttendanceRecordResponse toResponse(AttendanceRecord record);
}