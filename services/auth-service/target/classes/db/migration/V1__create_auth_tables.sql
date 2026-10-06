-- V1__create_auth_tables.sql
-- Authentication tables for 360 Church Flow

-- ── Roles ─────────────────────────────────────────────────────────────────────
-- Four church roles in ascending privilege order:
--   MEMBER   → basic access (attendance, events, own profile)
--   LEADER   → MEMBER + manage small groups, create events
--   PASTOR   → LEADER + pastoral meetings, finance reports
--   ADMIN    → full access including user management and all reports
CREATE TYPE user_role AS ENUM ('MEMBER', 'LEADER', 'PASTOR', 'ADMIN');

CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION');

-- ── Users ──────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
                                     id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Linked to member-service (optional — internal accounts may not have one)
                                     membership_number   VARCHAR(50)  UNIQUE,

    -- Credentials
                                     username            VARCHAR(100) NOT NULL UNIQUE,
                                     email               VARCHAR(255) NOT NULL UNIQUE,
                                     password_hash       VARCHAR(255) NOT NULL,

    -- Identity
                                     full_name           VARCHAR(255) NOT NULL,
                                     phone               VARCHAR(20),

    -- Authorization
                                     role                user_role    NOT NULL DEFAULT 'MEMBER',

    -- Account state
                                     status              account_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
                                     email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
                                     email_verified_at   TIMESTAMP,

    -- Password management
                                     password_changed_at TIMESTAMP,
                                     must_change_password BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Tracking
                                     last_login_at       TIMESTAMP,
                                     last_login_ip       VARCHAR(45),
                                     failed_login_attempts INTEGER    NOT NULL DEFAULT 0,
                                     locked_until        TIMESTAMP,

    -- Audit
                                     created_by          UUID,
                                     created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
                                     updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email            ON users(email);
CREATE INDEX idx_users_username         ON users(username);
CREATE INDEX idx_users_membership       ON users(membership_number);
CREATE INDEX idx_users_role             ON users(role);
CREATE INDEX idx_users_status           ON users(status);

-- ── Refresh Tokens ─────────────────────────────────────────────────────────────
-- Stored server-side for rotation and revocation.
-- A user can have multiple active refresh tokens (multiple devices).
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                              user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                              token_hash  VARCHAR(255) NOT NULL UNIQUE,  -- SHA-256 of the raw token
                                              device_info VARCHAR(500),                  -- user-agent / device name
                                              ip_address  VARCHAR(45),
                                              issued_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                                              expires_at  TIMESTAMP    NOT NULL,
                                              revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
                                              revoked_at  TIMESTAMP
);

CREATE INDEX idx_refresh_token_user    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_hash    ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);

-- ── Seed: default ADMIN account ────────────────────────────────────────────────
-- Password: Admin@360Church! (BCrypt hash — change on first login)
INSERT INTO users (
    username, email, password_hash, full_name,
    role, status, email_verified, must_change_password
) VALUES (
             'admin',
             'admin@360church.org',
             '$2a$12$NsXtBBIXeG5HupAsyAJQs.ePUXdCL9ja88V61LJT/SPHnAQiygwuO',
             '360 Church Administrator',
             'ADMIN',
             'ACTIVE',
             true,
             true
         );