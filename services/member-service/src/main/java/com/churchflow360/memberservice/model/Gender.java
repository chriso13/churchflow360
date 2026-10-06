package com.churchflow360.memberservice.model;

/**
 * Must match the PostgreSQL ENUM type defined in V2__create_members.sql:
 * CREATE TYPE gender AS ENUM ('MALE', 'FEMALE');
 */
public enum Gender {
    MALE,
    FEMALE
}