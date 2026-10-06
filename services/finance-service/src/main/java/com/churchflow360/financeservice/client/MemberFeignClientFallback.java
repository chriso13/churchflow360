package com.churchflow360.financeservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberFeignClientFallback implements MemberFeignClient {

    @Override
    public Object getMemberByMembershipNumber(String membershipNumber) {
        log.warn("member-service unavailable — fallback for: {}", membershipNumber);
        return null;
    }
}