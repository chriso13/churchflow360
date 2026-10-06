package com.churchflow360.pastoralservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation of {@link MemberFeignClient}.
 *
 * <p>Invoked by  when:
 * <ul>
 *   <li>The circuit breaker is OPEN (member-service has been failing)</li>
 *   <li>All retry attempts have been exhausted</li>
 *   <li>A non-retryable exception occurs</li>
 * </ul>
 *
 * <p>Returns {@code null} to signal "unavailable" to ,
 * which then logs a warning and allows the operation to proceed gracefully
 * rather than crashing the pastoral-meeting-service.
 */
@Component
@Slf4j
public class MemberFeignClientFallback implements MemberFeignClient {

    @Override
    public Object getMemberByMembershipNumber(String membershipNumber) {
        log.warn("member-service is unavailable — fallback triggered for membership number: {}. " +
                "Circuit breaker is OPEN or retries exhausted. " +
                "Proceeding without member verification.", membershipNumber);
        return null;
    }
}