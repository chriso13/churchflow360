package com.churchflow360.authservice.dto.request;

import com.churchflow360.authservice.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username may only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-]).{8,}$",
            message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    private String phone;

    // Optional — links this auth account to an existing church member
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format")
    private String membershipNumber;

    // Only ADMIN can set role; defaults to MEMBER if not provided
    private Role role;
}