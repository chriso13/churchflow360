package com.churchflow360.prayerrequest.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AuthClientFallback implements AuthClient {

    @Override
    public ResponseEntity<Map<String, Object>> getPastorUsers(int page, int size) {
        // If auth-service is unavailable, return null — caller handles gracefully
        return null;
    }
}
