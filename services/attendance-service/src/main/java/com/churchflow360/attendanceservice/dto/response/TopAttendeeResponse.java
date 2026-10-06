package com.churchflow360.attendanceservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopAttendeeResponse {
    private String membershipNumber;
    private String memberName;
    private long totalAttendance;
}