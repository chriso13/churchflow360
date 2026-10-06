package com.churchflow360.eventservice.security;

import lombok.Builder;
import lombok.Data;

/**
 * Authenticated caller identity forwarded by the API Gateway as X-Auth-* headers.
 *
 * Access in controllers via:
 * {@literal @}AuthenticationPrincipal GatewayAuthPrincipal principal
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
     * True when the caller is accessing their own RSVP or member record.
     */
    public boolean isOwner(String targetMembershipNumber) {
        return membershipNumber != null
                && !membershipNumber.isBlank()
                && membershipNumber.equalsIgnoreCase(targetMembershipNumber);
    }
}