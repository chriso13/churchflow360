package com.churchflow360.memberservice.dto.request;


import com.churchflow360.memberservice.model.MemberStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeStatusRequest {

    @NotNull(message = "New status is required")
    private MemberStatus newStatus;

    @Size(max = 500)
    private String reason;
}