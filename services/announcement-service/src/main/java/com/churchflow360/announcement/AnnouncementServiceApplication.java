package com.churchflow360.announcement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
@SpringBootApplication @EnableJpaAuditing
public class AnnouncementServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AnnouncementServiceApplication.class, args); }
}
