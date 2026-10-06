package com.churchflow360.authservice.dto.response;

import lombok.Data;

@Data
public class UserSummaryResponse {
    private java.util.UUID id;
    private String fullName;
    private String membershipNumber;
}