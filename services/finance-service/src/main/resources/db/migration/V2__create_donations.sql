-- V2__create_donations.sql
-- Donation records — open giving, no member account required.
-- Anonymous donations and non-member gifts are fully supported.
-- Donations target a specific fund or project.

CREATE TYPE donation_category AS ENUM (
    'BUILDING_FUND',
    'MISSIONS',
    'BENEVOLENCE',
    'YOUTH_MINISTRY',
    'CHILDREN_MINISTRY',
    'MEDIA_MINISTRY',
    'OUTREACH',
    'SCHOLARSHIP',
    'DISASTER_RELIEF',
    'GENERAL'
    );

CREATE TABLE IF NOT EXISTS donations (
                                         id                  UUID              PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Donor info — optional for anonymous giving
    -- If membership_number is provided it must exist in member-service
                                         membership_number   VARCHAR(50),       -- NULL = anonymous donor
                                         donor_name          VARCHAR(255),      -- free text (non-member name or alias)
                                         donor_email         VARCHAR(255),      -- for receipt sending

    -- Amount
                                         amount              NUMERIC(12,2)     NOT NULL CHECK (amount > 0),
                                         currency            VARCHAR(3)        NOT NULL DEFAULT 'USD',

    -- Classification
                                         category            donation_category NOT NULL DEFAULT 'GENERAL',
                                         fund_name           VARCHAR(255),      -- specific project name if any
                                         is_anonymous        BOOLEAN           NOT NULL DEFAULT FALSE,
                                         is_recurring        BOOLEAN           NOT NULL DEFAULT FALSE,

    -- When and how
                                         payment_date        DATE              NOT NULL,
                                         payment_method      payment_method    NOT NULL,
                                         reference_number    VARCHAR(100),

    -- Tax receipt
                                         receipt_number      VARCHAR(100)      UNIQUE,
                                         receipt_issued      BOOLEAN           NOT NULL DEFAULT FALSE,
                                         receipt_issued_at   TIMESTAMP,

                                         notes               TEXT,

    -- Audit
                                         recorded_by         VARCHAR(50)       NOT NULL,
                                         created_at          TIMESTAMP         NOT NULL DEFAULT NOW(),
                                         updated_at          TIMESTAMP         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_donations_member   ON donations(membership_number);
CREATE INDEX idx_donations_date     ON donations(payment_date);
CREATE INDEX idx_donations_category ON donations(category);
CREATE INDEX idx_donations_anonymous ON donations(is_anonymous);
CREATE INDEX idx_donations_receipt  ON donations(receipt_issued);