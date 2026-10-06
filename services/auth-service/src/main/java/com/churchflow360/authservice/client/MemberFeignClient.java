package com.churchflow360.authservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for member-service.
 *
 * Uses Eureka service discovery — resolves lb://member-service
 * to the registered member-service instance automatically.
 *
 * The endpoint called is GET /api/v1/members/number/{membershipNumber}
 * which is the same endpoint used by other services for member verification.
 *
 * Note: This call goes DIRECT to member-service (not through the gateway)
 * so it does NOT need a JWT. The member-service internal check still applies
 * — if member-service has security, add an internal service-to-service header.
 */
@FeignClient(
        name     = "member-service",
        path     = "/api/v1/members"
)
public interface MemberFeignClient {

    /**
     * Fetches a member by membership number.
     * Returns the member response object on 200, throws FeignException.NotFound on 404.
     */
    @GetMapping("/number/{membershipNumber}")
    Object getMemberByMembershipNumber(@PathVariable("membershipNumber") String membershipNumber);
}