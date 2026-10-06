-- V1__create_service_events.sql
-- Church services and events that attendance is tracked against

CREATE TYPE service_type AS ENUM (
    'SUNDAY_SERVICE',
    'OTHER_SERVICE',
    'BIBLE_STUDY',
    'PRAYER_MEETING',
    'YOUTH_SERVICE',
    'SPECIAL_EVENT'
    );

CREATE TABLE IF NOT EXISTS service_events (
                                              id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                              title           VARCHAR(200) NOT NULL,
                                              service_type    service_type NOT NULL,
                                              service_date    DATE         NOT NULL,
                                              start_time      TIME,
                                              end_time        TIME,
                                              location        VARCHAR(255),
                                              description     TEXT,
                                              qr_token        VARCHAR(100) UNIQUE,
                                              qr_expires_at   TIMESTAMP,
                                              created_by      UUID,
                                              created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                                              updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_events_date ON service_events(service_date);
CREATE INDEX idx_service_events_type ON service_events(service_type);
CREATE INDEX idx_service_events_qr   ON service_events(qr_token);