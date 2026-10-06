package com.churchflow360.attendanceservice.dto.response;

import com.churchflow360.attendanceservice.model.CheckInMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceRecordResponse {
    private UUID id;
    private UUID serviceEventId;
    private String serviceEventTitle;
    private String membershipNumber;
    private String memberName;
    private CheckInMethod checkinMethod;
    private LocalDateTime checkedInAt;
    private String notes;
}