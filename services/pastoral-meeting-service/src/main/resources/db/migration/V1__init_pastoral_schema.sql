-- V1__create_pastoral_meetings.sql
-- Pastoral care meetings between pastors and members

CREATE TYPE meeting_status AS ENUM (
    'REQUESTED',
    'CONFIRMED',
    'RESCHEDULED',
    'COMPLETED',
    'CANCELLED',
    'NO_SHOW'
    );

CREATE TYPE meeting_type AS ENUM (
    'COUNSELING',
    'DISCIPLESHIP',
    'PRAYER',
    'GENERAL_CARE',
    'CRISIS_SUPPORT',
    'FOLLOW_UP',
    'FAMILY_COUNSELING',
    'PREMARITAL',
    'GRIEF_SUPPORT'
    );

CREATE TABLE IF NOT EXISTS pastoral_meetings (
                                                 id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Pastor who leads the meeting (identified by membership number)
                                                 pastor_membership_number  VARCHAR(50)  NOT NULL,
                                                 pastor_name               VARCHAR(255),
    -- Member receiving pastoral care
                                                 member_membership_number  VARCHAR(50)  NOT NULL,
                                                 member_name               VARCHAR(255),
    -- Scheduling
                                                 meeting_type        meeting_type NOT NULL DEFAULT 'GENERAL_CARE',
                                                 status              meeting_status NOT NULL DEFAULT 'REQUESTED',
                                                 scheduled_date      DATE,
                                                 scheduled_time      TIME,
                                                 duration_minutes    INT          DEFAULT 60,
                                                 location            VARCHAR(255),
    -- Notes (visible to both)
                                                 subject             VARCHAR(500),
                                                 request_notes       TEXT,       -- member's reason for requesting
    -- Reschedule tracking
                                                 rescheduled_date    DATE,
                                                 rescheduled_time    TIME,
                                                 reschedule_reason   TEXT,
    -- Completion
                                                 completed_at        TIMESTAMP,
    -- Audit
                                                 requested_by        VARCHAR(50) NOT NULL,  -- membership number who created the request
                                                 created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
                                                 updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meetings_pastor  ON pastoral_meetings(pastor_membership_number);
CREATE INDEX idx_meetings_member  ON pastoral_meetings(member_membership_number);
CREATE INDEX idx_meetings_status  ON pastoral_meetings(status);
CREATE INDEX idx_meetings_date    ON pastoral_meetings(scheduled_date);