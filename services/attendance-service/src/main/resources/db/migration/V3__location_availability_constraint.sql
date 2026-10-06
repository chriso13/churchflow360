-- =============================================================================
-- Attendance Service — Location Availability Constraint
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Remove any duplicate overlapping events before adding the constraint
-- (keeps the earliest created event when duplicates exist)
DELETE FROM service_events
WHERE id NOT IN (
    SELECT MIN(id::text)::uuid
    FROM service_events
    GROUP BY LOWER(location), service_date, start_time, end_time
);

-- Add computed time-range column
ALTER TABLE service_events
    ADD COLUMN IF NOT EXISTS time_range tsrange
        GENERATED ALWAYS AS (
            tsrange(
                service_date + start_time,
                service_date + end_time,
                '[)'
            )
        ) STORED;

-- Exclusion constraint — no two events can share the same location
-- with overlapping time ranges
ALTER TABLE service_events
    ADD CONSTRAINT no_location_overlap
    EXCLUDE USING gist (
        LOWER(location) WITH =,
        time_range      WITH &&
    )
    WHERE (location IS NOT NULL AND start_time IS NOT NULL AND end_time IS NOT NULL);

-- Index to speed up the overlap query used by the service layer
CREATE INDEX IF NOT EXISTS idx_service_events_location_date
    ON service_events (LOWER(location), service_date);
