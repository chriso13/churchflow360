package com.churchflow360.memberservice.dto.request;


import com.churchflow360.memberservice.model.Gender;
import com.churchflow360.memberservice.model.MaritalStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateMemberRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,30}$", message = "Invalid phone number format")
    private String phone;

    @Past
    private LocalDate dateOfBirth;

    private Gender gender;

    private MaritalStatus maritalStatus;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    private String occupation;
    private String notes;

    private LocalDate joinDate;
    private LocalDate baptismDate;
    private Boolean waterBaptized;
    private Boolean holySpirit;
    private Boolean titheMember;

    private UUID familyGroupId;
    private UUID smallGroupId;
    private UUID invitedById;
}