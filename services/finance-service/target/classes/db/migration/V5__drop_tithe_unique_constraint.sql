-- Allows a member to record multiple tithes in the same month.
-- The original V1 migration created this as a unique INDEX (not a constraint),
-- so it must be dropped with DROP INDEX, not DROP CONSTRAINT.
DROP INDEX IF EXISTS uq_tithe_member_period;
