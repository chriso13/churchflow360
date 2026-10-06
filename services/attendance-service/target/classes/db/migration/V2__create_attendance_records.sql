-- V2__create_attendance_records.sql
-- Individual check-in records per member per service

CREATE TYPE checkin_method AS ENUM ('QR_CODE', 'MANUAL', 'SELF_CHECKIN');

CREATE TABLE IF NOT EXISTS attendance_records (
                                                  id                 UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  service_event_id   UUID           NOT NULL REFERENCES service_events(id) ON DELETE CASCADE,
                                                  membership_number  VARCHAR(50)    NOT NULL,
                                                  member_name        VARCHAR(255),
                                                  checkin_method     checkin_method NOT NULL DEFAULT 'MANUAL',
                                                  checked_in_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
                                                  checked_in_by      UUID,
                                                  notes              TEXT,
    -- Prevent duplicate check-ins for same member in same service
                                                  CONSTRAINT uq_attendance_member_service UNIQUE (service_event_id, membership_number)
);

CREATE INDEX idx_attendance_service        ON attendance_records(service_event_id);
CREATE INDEX idx_attendance_membership_num ON attendance_records(membership_number);
CREATE INDEX idx_attendance_date           ON attendance_records(checked_in_at);
CREATE INDEX idx_attendance_method         ON attendance_records(checkin_method);