package com.churchflow360.smallgroups.dto.response;

import com.churchflow360.smallgroups.model.MemberRole;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GroupMembershipResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String membershipNumber;
    private String memberName;
    private MemberRole role;
    private LocalDate joinedDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
