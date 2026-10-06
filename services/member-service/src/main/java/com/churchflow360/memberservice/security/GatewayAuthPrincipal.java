package com.churchflow360.memberservice.security;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the authenticated caller as identified by the API Gateway.
 *
 * <p>Access this in controllers via:
 * <pre>
 * {@literal @}AuthenticationPrincipal GatewayAuthPrincipal principal
 * </pre>
 */
@Data
@Builder
public class GatewayAuthPrincipal {

    private String userId;
    private String username;
    private String role;
    private String email;
    private String membershipNumber;

    public boolean isAdmin()  { return "ADMIN".equals(role); }
    public boolean isPastor() { return "PASTOR".equals(role) || isAdmin(); }
    public boolean isLeader() { return "LEADER".equals(role) || isPastor(); }
    public boolean isMember() { return "MEMBER".equals(role) || isLeader(); }

    /**
     * Returns true when the caller is accessing their own membership record.
     * Used to allow MEMBER role to view/update their own profile only.
     */
    public boolean isOwner(String targetMembershipNumber) {
        return membershipNumber != null
                && !membershipNumber.isBlank()
                && membershipNumber.equalsIgnoreCase(targetMembershipNumber);
    }
}