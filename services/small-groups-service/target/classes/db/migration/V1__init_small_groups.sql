-- ============================================================
-- Small Groups Service — Initial Schema
-- ============================================================

CREATE TYPE group_type AS ENUM (
    'CELL_GROUP','BIBLE_STUDY','YOUTH_GROUP','WOMENS_GROUP','MENS_GROUP',
    'PRAYER_GROUP','OUTREACH_TEAM','WORSHIP_TEAM','CHILDREN_MINISTRY','DISCIPLESHIP','OTHER'
);

CREATE TYPE group_status AS ENUM ('ACTIVE','INACTIVE','FULL','FORMING');

CREATE TYPE member_role AS ENUM ('LEADER','CO_LEADER','MEMBER','APPRENTICE');

CREATE TYPE meeting_frequency AS ENUM ('WEEKLY','BIWEEKLY','MONTHLY','AS_NEEDED');

CREATE TABLE small_groups (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(150) NOT NULL,
    description             TEXT,
    group_type              group_type NOT NULL DEFAULT 'CELL_GROUP',
    status                  group_status NOT NULL DEFAULT 'ACTIVE',
    meeting_frequency       meeting_frequency,
    meeting_day             VARCHAR(20),
    meeting_time            TIME,
    meeting_location        VARCHAR(255),
    max_members             INT NOT NULL DEFAULT 20,
    leader_membership_number VARCHAR(50) NOT NULL,
    leader_name             VARCHAR(255),
    pastor_membership_number VARCHAR(50),
    target_area             VARCHAR(150),
    notes                   TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE group_memberships (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id          UUID NOT NULL REFERENCES small_groups(id) ON DELETE CASCADE,
    membership_number VARCHAR(50) NOT NULL,
    member_name       VARCHAR(255),
    role              member_role NOT NULL DEFAULT 'MEMBER',
    joined_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    is_active         BOOLEAN NOT NULL DEFAULT true,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_group_member UNIQUE (group_id, membership_number)
);

CREATE INDEX idx_groups_status       ON small_groups(status);
CREATE INDEX idx_groups_type         ON small_groups(group_type);
CREATE INDEX idx_groups_leader       ON small_groups(leader_membership_number);
CREATE INDEX idx_memberships_group   ON group_memberships(group_id);
CREATE INDEX idx_memberships_member  ON group_memberships(membership_number);
CREATE INDEX idx_memberships_active  ON group_memberships(is_active);
