-- Prayer Request Service — Initial Schema

CREATE TYPE request_category AS ENUM ('HEALING','FAMILY','FINANCIAL','SPIRITUAL_GROWTH','RELATIONSHIPS','GRIEF','SALVATION','GUIDANCE','THANKSGIVING','OTHER');
CREATE TYPE privacy_level    AS ENUM ('PUBLIC','LEADERS_ONLY','PASTOR_ONLY','PRIVATE');
CREATE TYPE request_status   AS ENUM ('ACTIVE','ANSWERED','ONGOING','ARCHIVED');
CREATE TYPE urgency_level    AS ENUM ('LOW','NORMAL','HIGH','URGENT');

CREATE TABLE prayer_requests (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_membership_number VARCHAR(50),
    requester_name              VARCHAR(255),
    title                       VARCHAR(255) NOT NULL,
    request_text                TEXT NOT NULL,
    category                    request_category NOT NULL DEFAULT 'OTHER',
    privacy_level               privacy_level    NOT NULL DEFAULT 'PUBLIC',
    status                      request_status   NOT NULL DEFAULT 'ACTIVE',
    urgency                     urgency_level    NOT NULL DEFAULT 'NORMAL',
    is_anonymous                BOOLEAN NOT NULL DEFAULT false,
    prayer_count                INT NOT NULL DEFAULT 0,
    answered_testimony          TEXT,
    answered_at                 TIMESTAMP,
    assigned_pastor_number      VARCHAR(50),
    expires_at                  TIMESTAMP,
    created_at                  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_prayer_status    ON prayer_requests(status);
CREATE INDEX idx_prayer_category  ON prayer_requests(category);
CREATE INDEX idx_prayer_privacy   ON prayer_requests(privacy_level);
CREATE INDEX idx_prayer_requester ON prayer_requests(requester_membership_number);
CREATE INDEX idx_prayer_urgency   ON prayer_requests(urgency);
