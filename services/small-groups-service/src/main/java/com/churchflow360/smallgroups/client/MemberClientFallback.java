package com.churchflow360.smallgroups.client;

import com.churchflow360.smallgroups.exception.MemberNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MemberClientFallback implements MemberClient {

    @Override
    public ResponseEntity<Map<String, Object>> getMemberByMembershipNumber(
            String membershipNumber) {
        throw new MemberNotFoundException(
                "Member service unavailable. Cannot verify: " + membershipNumber);
    }
}
