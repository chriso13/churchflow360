package com.churchflow360.memberservice.dto.request;


import com.churchflow360.memberservice.model.Gender;
import com.churchflow360.memberservice.model.MaritalStatus;
import com.churchflow360.memberservice.model.MemberStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MemberRequest {


    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Email(message = "A valid email address is required")
    @Size(max = 255)
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,30}$", message = "Invalid phone number format")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    private MaritalStatus maritalStatus;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 100)
    private String country;

    @Size(max = 150)
    private String occupation;

    private String notes;

    private MemberStatus membershipStatus;

    private LocalDate joinDate;

    private LocalDate baptismDate;

}