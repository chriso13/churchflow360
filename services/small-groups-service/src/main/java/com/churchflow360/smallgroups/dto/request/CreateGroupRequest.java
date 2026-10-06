package com.churchflow360.smallgroups.dto.request;

import com.churchflow360.smallgroups.model.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalTime;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @NotNull(message = "Group type is required")
    private GroupType groupType;

    private MeetingFrequency meetingFrequency;
    private String meetingDay;
    private LocalTime meetingTime;
    private String meetingLocation;

    @Min(2) @Max(200)
    private Integer maxMembers = 20;

    @NotBlank(message = "Leader membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
             message = "Invalid membership number format")
    private String leaderMembershipNumber;

    private String leaderName;
    private String targetArea;
    private String notes;
}
