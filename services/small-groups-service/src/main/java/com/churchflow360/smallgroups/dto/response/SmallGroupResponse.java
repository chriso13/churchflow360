package com.churchflow360.smallgroups.dto.response;

import com.churchflow360.smallgroups.model.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class SmallGroupResponse {
    private UUID id;
    private String name;
    private String description;
    private GroupType groupType;
    private GroupStatus status;
    private MeetingFrequency meetingFrequency;
    private String meetingDay;
    private LocalTime meetingTime;
    private String meetingLocation;
    private Integer maxMembers;
    private Long currentMemberCount;
    private String leaderMembershipNumber;
    private String leaderName;
    private String targetArea;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
