package com.churchflow360.memberservice.model;

/**
 * Membership status values.
 * Must match the PostgreSQL ENUM type defined in V2__create_members.sql:
 * CREATE TYPE member_status AS ENUM ('ACTIVE','INACTIVE','VISITOR','TRANSFERRED','DECEASED');
 */
public enum MemberStatus {
    ACTIVE,
    INACTIVE,
    VISITOR,
    TRANSFERRED,
    DECEASED
}