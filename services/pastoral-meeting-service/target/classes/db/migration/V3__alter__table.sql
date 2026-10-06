-- ============================================================================
-- Add preferred_date and preferred_time to pastoral_meetings
-- These hold the date/time the MEMBER requested. The pastor then sets the
-- official scheduled_date/scheduled_time when confirming the meeting.
-- ============================================================================

ALTER TABLE pastoral_meetings
    ADD COLUMN IF NOT EXISTS preferred_date DATE;

ALTER TABLE pastoral_meetings
    ADD COLUMN IF NOT EXISTS preferred_time TIME;