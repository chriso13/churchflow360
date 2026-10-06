-- V3__create_offerings.sql
-- Offering records — giving collected during a specific worship service or event.
-- Linked to a service date and type. Individual givers are optional.

CREATE TYPE offering_type AS ENUM (
    'SUNDAY_OFFERING',
    'MIDWEEK_OFFERING',
    'SPECIAL_OFFERING',
    'HARVEST_OFFERING',
    'THANKSGIVING_OFFERING',
    'FIRST_FRUIT',
    'LOVE_OFFERING',
    'MISSIONS_OFFERING'
    );

CREATE TABLE IF NOT EXISTS offerings (
                                         id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Service/worship link
                                         service_date        DATE           NOT NULL,
                                         service_description VARCHAR(255),   -- e.g. "Sunday Morning Service — June 9, 2026"
                                         offering_type       offering_type  NOT NULL DEFAULT 'SUNDAY_OFFERING',

    -- Total collected (sum of all envelopes/plates for this service)
                                         total_amount        NUMERIC(12,2)  NOT NULL CHECK (total_amount >= 0),
                                         currency            VARCHAR(3)     NOT NULL DEFAULT 'USD',

    -- Breakdown by payment method (denormalized for quick reporting)
                                         cash_amount         NUMERIC(12,2)  NOT NULL DEFAULT 0,
                                         check_amount        NUMERIC(12,2)  NOT NULL DEFAULT 0,
                                         digital_amount      NUMERIC(12,2)  NOT NULL DEFAULT 0,

    -- Count of individual givers (even if anonymous)
                                         giver_count         INTEGER        NOT NULL DEFAULT 0,

    -- Counted and verified by
                                         counted_by          VARCHAR(255),
                                         verified_by         VARCHAR(255),
                                         verified_at         TIMESTAMP,

                                         notes               TEXT,

    -- Audit
                                         recorded_by         VARCHAR(50)    NOT NULL,
                                         created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
                                         updated_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Individual envelope/line items within an offering (optional detail)
CREATE TABLE IF NOT EXISTS offering_items (
                                              id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                              offering_id         UUID           NOT NULL REFERENCES offerings(id) ON DELETE CASCADE,

    -- Giver — optional (some envelopes are anonymous)
                                              membership_number   VARCHAR(50),
                                              giver_name          VARCHAR(255),
                                              is_anonymous        BOOLEAN        NOT NULL DEFAULT FALSE,

                                              amount              NUMERIC(12,2)  NOT NULL CHECK (amount > 0),
                                              payment_method      payment_method NOT NULL,
                                              envelope_number     VARCHAR(50),
                                              notes               TEXT,

                                              created_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_offerings_date    ON offerings(service_date);
CREATE INDEX idx_offerings_type    ON offerings(offering_type);
CREATE INDEX idx_offering_items_offering ON offering_items(offering_id);
CREATE INDEX idx_offering_items_member   ON offering_items(membership_number);