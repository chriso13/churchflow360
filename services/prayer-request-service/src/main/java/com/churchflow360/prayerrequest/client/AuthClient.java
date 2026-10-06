package com.churchflow360.prayerrequest.client;

import com.churchflow360.prayerrequest.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(
    name = "auth-service",
    configuration = FeignConfig.class,
    fallback = AuthClientFallback.class
)
public interface AuthClient {

    // Gets users filtered by role — used to verify PASTOR role
    @GetMapping("/api/v1/users/role/PASTOR")
    ResponseEntity<Map<String, Object>> getPastorUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "100") int size);
}
