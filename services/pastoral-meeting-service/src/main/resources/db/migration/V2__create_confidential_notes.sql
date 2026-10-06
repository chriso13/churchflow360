-- V2__create_confidential_notes.sql
-- Confidential pastoral notes — accessible ONLY by the pastor who wrote them.
-- These notes are NEVER returned in meeting responses.
-- A separate secured endpoint /pastoral-meetings/{id}/notes requires PASTOR role.

CREATE TABLE IF NOT EXISTS confidential_notes (
                                                  id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  meeting_id  UUID         NOT NULL REFERENCES pastoral_meetings(id) ON DELETE CASCADE,
    -- Author of the note (must be the pastor of the meeting)
                                                  author_membership_number VARCHAR(50) NOT NULL,
                                                  author_name              VARCHAR(255),
    -- The confidential content
                                                  content     TEXT         NOT NULL,
    -- Follow-up action items the pastor sets for themselves
                                                  follow_up_required  BOOLEAN      DEFAULT FALSE,
                                                  follow_up_by        DATE,
                                                  follow_up_notes     TEXT,
                                                  created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                                  updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notes_meeting ON confidential_notes(meeting_id);
CREATE INDEX idx_notes_author  ON confidential_notes(author_membership_number);
CREATE INDEX idx_notes_followup ON confidential_notes(follow_up_required, follow_up_by);


