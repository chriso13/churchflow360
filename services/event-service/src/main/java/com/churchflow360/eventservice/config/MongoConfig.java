package com.churchflow360.eventservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.churchflow360.eventservice.repository")
public class MongoConfig {
    // MongoDB auditing enables @CreatedDate and @LastModifiedDate on documents
}