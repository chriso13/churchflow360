package com.churchflow360.eventservice.client;

import com.churchflow360.eventservice.exception.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that verifies a membership number exists in the member-service.
 *
 * <p>Calls: GET http://member-service:8082/api/v1/members/number/{membershipNumber}
 * If the member-service returns 404 the membership number does not exist and
 * the operation is rejected with a clear error message.
 */
@Component
@Slf4j
public class MemberClient {

    private final RestTemplate restTemplate;
    private final String memberServiceUrl;

    public MemberClient(
            RestTemplate restTemplate,
            @Value("${churchflow360.member-service.url:http://localhost:8082}") String memberServiceUrl) {
        this.restTemplate     = restTemplate;
        this.memberServiceUrl = memberServiceUrl;
    }

    /**
     * Verifies that the given membership number exists in member-service.
     *
     * @param membershipNumber e.g. MEM-0609-2026-113045
     * @throws MemberNotFoundException if the member does not exist
     */
    public void verifyMemberExists(String membershipNumber) {
        String url = memberServiceUrl + "/api/v1/members/number/" + membershipNumber.toUpperCase();
        try {
            restTemplate.getForObject(url, Object.class);
            log.debug("Member verified: {}", membershipNumber);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Member not found: {}", membershipNumber);
            throw new MemberNotFoundException(
                    "Membership number '" + membershipNumber + "' does not exist. " +
                            "Please register as a member before performing this action.");
        } catch (Exception ex) {
            // If member-service is temporarily unreachable, log warning but allow the
            // operation to proceed — avoids cascading failures when member-service is down.
            log.warn("Could not verify member {} — member-service unreachable: {}",
                    membershipNumber, ex.getMessage());
        }
    }
}