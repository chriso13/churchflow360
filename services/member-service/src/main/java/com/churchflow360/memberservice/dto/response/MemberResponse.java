package com.churchflow360.memberservice.dto.response;

import com.churchflow360.memberservice.model.Gender;
import com.churchflow360.memberservice.model.MaritalStatus;
import com.churchflow360.memberservice.model.MemberStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MemberResponse {

    private UUID id;
    private String membershipNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String profilePhoto;
    private String occupation;
    private String notes;
    private MemberStatus membershipStatus;
    private LocalDate joinDate;
    private LocalDate baptismDate;
    private Boolean waterBaptized;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}