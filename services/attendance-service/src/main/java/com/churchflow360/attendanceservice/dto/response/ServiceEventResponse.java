package com.churchflow360.attendanceservice.dto.response;

import com.churchflow360.attendanceservice.model.ServiceType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ServiceEventResponse {
    private UUID id;
    private String title;
    private ServiceType serviceType;
    private LocalDate serviceDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String description;
    private String qrToken;
    private LocalDateTime qrExpiresAt;
    private boolean qrValid;
    private int totalAttendance;
    private LocalDateTime createdAt;
}