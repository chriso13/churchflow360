package com.churchflow360.attendanceservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AttendanceStatsResponse {
    private LocalDate from;
    private LocalDate to;
    private long totalServices;
    private long totalCheckIns;
    private double averagePerService;
    private long uniqueMembers;
    private int highestAttendance;
    private int lowestAttendance;
    private List<TopAttendeeResponse> topAttendees;
}