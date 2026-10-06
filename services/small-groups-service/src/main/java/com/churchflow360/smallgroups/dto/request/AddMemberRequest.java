package com.churchflow360.smallgroups.dto.request;

import com.churchflow360.smallgroups.model.MemberRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotBlank(message = "Membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
             message = "Invalid membership number format")
    private String membershipNumber;

    private String memberName;
    private MemberRole role = MemberRole.MEMBER;
}
