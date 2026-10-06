package com.churchflow360.memberservice.model;

/**
 * Must match the PostgreSQL ENUM type defined in V2__create_members.sql:
 * CREATE TYPE marital_status AS ENUM ('SINGLE','MARRIED','DIVORCED','WIDOWED');
 */
public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED
}