-- V3__create_member_status_history.sql
-- Audit trail for membership status changes

CREATE TABLE IF NOT EXISTS member_status_history (
                                                     id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                                     member_id  UUID          NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                                     old_status member_status,
                                                     new_status member_status NOT NULL,
                                                     reason     TEXT,
                                                     changed_by UUID          NOT NULL,
                                                     changed_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status_history_member ON member_status_history(member_id);
CREATE INDEX idx_status_history_date   ON member_status_history(changed_at);