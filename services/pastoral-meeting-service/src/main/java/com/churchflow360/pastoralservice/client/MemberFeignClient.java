package com.churchflow360.pastoralservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative Feign client for member-service.
 *
 * <p>{@code name = "member-service"} tells Feign to resolve the host via
 * Eureka service discovery — no hardcoded URL needed. The load balancer
 * will pick a healthy instance automatically.
 *
 * <p>The {@code fallback} points to {@link MemberFeignClientFallback} which
 * is invoked by Resilience4j when the circuit is open or a call fails after
 * all retries are exhausted.
 */
@FeignClient(
        name     = "member-service",
        fallback = MemberFeignClientFallback.class
)
public interface MemberFeignClient {

    /**
     * Calls GET /api/v1/members/number/{membershipNumber} on member-service.
     * Returns the raw response body as an Object (we only care about success/failure).
     */
    @GetMapping("/api/v1/members/number/{membershipNumber}")
    Object getMemberByMembershipNumber(@PathVariable("membershipNumber") String membershipNumber);
}