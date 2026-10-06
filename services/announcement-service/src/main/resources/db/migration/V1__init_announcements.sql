-- Announcement Service — Initial Schema

CREATE TYPE announcement_category AS ENUM ('GENERAL','EVENTS','MINISTRY','FINANCIAL','PASTORAL','YOUTH','URGENT','OTHER');
CREATE TYPE audience_type        AS ENUM ('ALL_MEMBERS','LEADERS_ONLY','PASTORS_ONLY','SPECIFIC_GROUP');
CREATE TYPE announcement_status  AS ENUM ('DRAFT','PUBLISHED','ARCHIVED','SCHEDULED');
CREATE TYPE priority_level       AS ENUM ('LOW','NORMAL','HIGH','URGENT');

CREATE TABLE announcements (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title                    VARCHAR(255) NOT NULL,
    content                  TEXT NOT NULL,
    summary                  VARCHAR(500),
    category                 announcement_category NOT NULL DEFAULT 'GENERAL',
    audience                 audience_type         NOT NULL DEFAULT 'ALL_MEMBERS',
    status                   announcement_status   NOT NULL DEFAULT 'DRAFT',
    priority                 priority_level        NOT NULL DEFAULT 'NORMAL',
    target_group_id          UUID,
    published_at             TIMESTAMP,
    expires_at               TIMESTAMP,
    scheduled_at             TIMESTAMP,
    author_membership_number VARCHAR(50) NOT NULL,
    author_name              VARCHAR(255),
    image_url                VARCHAR(500),
    action_url               VARCHAR(500),
    action_label             VARCHAR(100),
    view_count               INT NOT NULL DEFAULT 0,
    pin_to_top               BOOLEAN NOT NULL DEFAULT false,
    created_at               TIMESTAMP NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ann_status    ON announcements(status);
CREATE INDEX idx_ann_category  ON announcements(category);
CREATE INDEX idx_ann_audience  ON announcements(audience);
CREATE INDEX idx_ann_priority  ON announcements(priority);
CREATE INDEX idx_ann_pin       ON announcements(pin_to_top);
CREATE INDEX idx_ann_expires   ON announcements(expires_at);
CREATE INDEX idx_ann_author    ON announcements(author_membership_number);
