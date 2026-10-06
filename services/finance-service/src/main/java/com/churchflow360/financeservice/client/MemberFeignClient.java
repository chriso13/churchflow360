package com.churchflow360.financeservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", fallback = MemberFeignClientFallback.class)
public interface MemberFeignClient {

    @GetMapping("/api/v1/members/number/{membershipNumber}")
    Object getMemberByMembershipNumber(@PathVariable("membershipNumber") String membershipNumber);
}