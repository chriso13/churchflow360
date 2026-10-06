package com.churchflow360.pastoralservice.client;

import com.churchflow360.pastoralservice.exception.MemberNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service-layer wrapper around {@link MemberFeignClient}.
 *
 * <p>Adds two Resilience4j protections:
 *
 * <p><b>@Retry("memberService")</b> — if the call throws a retryable exception
 * (connection timeout, 5xx), it is retried up to 3 times with a 500 ms wait
 * before the circuit breaker counts it as a failure.
 *
 * <p><b>@CircuitBreaker("memberService")</b> — tracks the failure rate of calls
 * to member-service. Once the failure rate exceeds 50% over a sliding window of
 * 10 calls, the circuit opens and all subsequent calls fail immediately (using
 * the fallback) for 30 seconds, giving member-service time to recover.
 *
 * <p>The Feign fallback ({@link MemberFeignClientFallback}) returns {@code null}
 * when the circuit is open. This wrapper treats {@code null} as "unavailable"
 * and logs a warning rather than throwing — preventing the pastoral-meeting-service
 * from cascading down when member-service is temporarily unreachable.
 *
 * <p>A {@link FeignException.NotFound} (HTTP 404) is NOT retried and is NOT
 * treated as a circuit breaker failure — it means the member genuinely does not
 * exist, and {@link MemberNotFoundException} is thrown immediately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberClient {

    private final MemberFeignClient memberFeignClient;

    private static final String CB_NAME = "memberService";

    /**
     * Verifies that a membership number exists in member-service.
     *
     * @param membershipNumber the membership number to verify (e.g. MEM-0609-2026-113045)
     * @throws MemberNotFoundException if the member does not exist (HTTP 404)
     */
    @Retry(name = CB_NAME, fallbackMethod = "verifyMemberFallback")
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "verifyMemberFallback")
    public void verifyMemberExists(String membershipNumber) {
        try {
            Object result = memberFeignClient.getMemberByMembershipNumber(
                    membershipNumber.toUpperCase());

            if (result == null) {
                // Feign fallback was triggered — member-service is unavailable
                log.warn("member-service fallback active for: {} — skipping verification",
                        membershipNumber);
                return;
            }

            log.debug("Member verified: {}", membershipNumber);

        } catch (FeignException.NotFound ex) {
            // 404 = member genuinely does not exist — reject immediately, no retry
            log.warn("Member not found: {}", membershipNumber);
            throw new MemberNotFoundException(
                    "Membership number '" + membershipNumber + "' does not exist. " +
                            "Please register as a member before performing this action.");
        }
    }

    /**
     * Fallback invoked by Resilience4j when all retries are exhausted
     * or the circuit breaker is OPEN.
     *
     * <p>Logs a warning and returns normally — the calling service operation
     * proceeds without member verification rather than failing entirely.
     */
    @SuppressWarnings("unused")
    public void verifyMemberFallback(String membershipNumber, Throwable ex) {
        log.warn("member-service unreachable after retries for: {} [{}]. " +
                "Proceeding without verification.", membershipNumber, ex.getMessage());
    }
}