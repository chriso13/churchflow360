package com.churchflow360.authservice.dto.request;

import com.churchflow360.authservice.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    private String reason;
}