package com.churchflow360.authservice.client;

import com.churchflow360.authservice.exception.MemberServiceUnavailableException;
import com.churchflow360.authservice.exception.MemberVerificationException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Verifies that a membership number exists in member-service
 * before allowing registration in auth-service.
 *
 * Outcomes:
 *  - HTTP 200  → member exists → registration proceeds.
 *  - HTTP 404  → member not found → throws MemberVerificationException (422).
 *  - Unreachable / circuit open → throws MemberServiceUnavailableException (503).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberClient {

    private final MemberFeignClient memberFeignClient;
    private static final String CB = "memberService";

    @Retry(name = CB, fallbackMethod = "verifyFallback")
    @CircuitBreaker(name = CB, fallbackMethod = "verifyFallback")
    public void verifyMemberExists(String membershipNumber) {
        try {
            Object result = memberFeignClient
                    .getMemberByMembershipNumber(membershipNumber.toUpperCase());

            if (result == null) {
                throw new MemberServiceUnavailableException(
                        "Member verification is temporarily unavailable. " +
                                "Please try again in a few minutes.");
            }

            log.debug("Membership number verified: {}", membershipNumber);

        } catch (FeignException.NotFound ex) {
            log.warn("Membership number not found in member-service: {}", membershipNumber);
            throw new MemberVerificationException(
                    "Membership number '" + membershipNumber + "' was not found. " +
                            "Please check your membership card or contact the church office at " +
                            "admin@360church.org to verify your membership number before registering.");
        }
    }

    @SuppressWarnings("unused")
    public void verifyFallback(String membershipNumber, Throwable ex) {
        // Traverse full cause chain — Resilience4j wraps the original exception
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof MemberVerificationException mex) throw mex;
            if (cause instanceof MemberServiceUnavailableException msex) throw msex;
            cause = cause.getCause();
        }

        // Genuine service-unavailable scenario — fail closed
        log.error("member-service unavailable for verification of {} — circuit may be OPEN",
                membershipNumber);
        throw new MemberServiceUnavailableException(
                "Member verification is temporarily unavailable. " +
                        "Please try again in a few minutes.");
    }
}