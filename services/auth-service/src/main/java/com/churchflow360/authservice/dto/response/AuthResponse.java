package com.churchflow360.authservice.dto.response;

import com.churchflow360.authservice.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String  accessToken;
    private String  refreshToken;
    private String  tokenType;
    private long    expiresIn;         // seconds
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private Role   role;
        private String membershipNumber;
        private boolean mustChangePassword;
    }
}