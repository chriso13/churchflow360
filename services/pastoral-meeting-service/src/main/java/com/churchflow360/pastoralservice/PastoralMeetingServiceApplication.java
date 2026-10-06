package com.churchflow360.pastoralservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @EnableFeignClients scans the client package and registers
 * MemberFeignClient as a Spring bean backed by a generated proxy.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.churchflow360.pastoralservice.client")
public class PastoralMeetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PastoralMeetingServiceApplication.class, args);
    }
}