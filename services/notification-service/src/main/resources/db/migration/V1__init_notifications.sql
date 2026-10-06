-- Notification Service — Initial Schema

CREATE TYPE notification_type    AS ENUM ('EMAIL', 'SMS', 'BOTH');
CREATE TYPE notification_status  AS ENUM ('PENDING', 'SENT', 'FAILED', 'SKIPPED');
CREATE TYPE notification_channel AS ENUM (
    'PRAYER_REQUEST', 'MEMBER_WELCOME', 'PASTORAL_MEETING',
    'ANNOUNCEMENT', 'TITHE_RECEIPT', 'MANUAL'
);

CREATE TABLE notification_logs (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_membership_number VARCHAR(50),
    recipient_name              VARCHAR(255),
    recipient_email             VARCHAR(255),
    recipient_phone             VARCHAR(30),
    type                        notification_type    NOT NULL,
    channel                     notification_channel NOT NULL,
    subject                     VARCHAR(255),
    body                        TEXT,
    email_status                notification_status DEFAULT 'PENDING',
    sms_status                  notification_status DEFAULT 'PENDING',
    email_error                 TEXT,
    sms_error                   TEXT,
    reference_id                VARCHAR(100),
    sent_at                     TIMESTAMP,
    created_at                  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notif_member  ON notification_logs(recipient_membership_number);
CREATE INDEX idx_notif_channel ON notification_logs(channel);
CREATE INDEX idx_notif_email_status ON notification_logs(email_status);
CREATE INDEX idx_notif_sms_status   ON notification_logs(sms_status);
CREATE INDEX idx_notif_created ON notification_logs(created_at DESC);
