package com.churchflow360.authservice.dto.response;

import com.churchflow360.authservice.model.AccountStatus;
import com.churchflow360.authservice.model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID          id;
    private String        username;
    private String        email;
    private String        fullName;
    private String        phone;
    private Role          role;
    private AccountStatus status;
    private String        membershipNumber;
    private Boolean       emailVerified;
    private Boolean       mustChangePassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}