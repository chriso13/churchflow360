package com.churchflow360.smallgroups.client;

import com.churchflow360.smallgroups.config.FeignConfig;
import com.churchflow360.smallgroups.exception.MemberNotFoundException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(
        name = "member-service",
        configuration = FeignConfig.class,
        fallback = MemberClientFallback.class
)
public interface MemberClient {

    // Correct path: /api/v1/members/number/{membershipNumber}
    @GetMapping("/api/v1/members/number/{membershipNumber}")
    ResponseEntity<Map<String, Object>> getMemberByMembershipNumber(
            @PathVariable("membershipNumber") String membershipNumber);

    default void verifyMemberExists(String membershipNumber) {
        try {
            ResponseEntity<Map<String, Object>> response =
                    getMemberByMembershipNumber(membershipNumber);
            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                throw new MemberNotFoundException(
                        "Member not found with membership number: " + membershipNumber);
            }
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new MemberNotFoundException(
                    "Member not found with membership number: " + membershipNumber);
        }
    }
}