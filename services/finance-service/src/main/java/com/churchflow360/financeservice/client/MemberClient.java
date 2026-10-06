package com.churchflow360.financeservice.client;

import com.churchflow360.financeservice.exception.MemberNotFoundException;
import com.churchflow360.financeservice.exception.MemberServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
                        "Member verification temporarily unavailable. " +
                        "Please try again in a moment.");
            }

            log.debug("Member verified: {}", membershipNumber);

        } catch (FeignException.NotFound ex) {
            log.warn("Member not found: {}", membershipNumber);
            throw new MemberNotFoundException(
                    "Membership number '" + membershipNumber + "' does not exist. " +
                    "Please verify the membership number and try again.");
        }
    }

    @SuppressWarnings("unused")
    public void verifyFallback(String membershipNumber, Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof MemberNotFoundException mex) throw mex;
            if (cause instanceof MemberServiceUnavailableException msex) throw msex;
            cause = cause.getCause();
        }
        log.error("member-service unavailable for {} — circuit may be OPEN", membershipNumber);
        throw new MemberServiceUnavailableException(
                "Member verification service is currently unavailable. " +
                "Cannot process this request without verifying membership number '" +
                membershipNumber + "'. Please try again later.");
    }
}
