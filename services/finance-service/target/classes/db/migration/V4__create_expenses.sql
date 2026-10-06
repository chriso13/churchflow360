-- V4__create_expenses.sql
-- Church expense tracking — all money going OUT of the church.
-- Supports approval workflow, budget categories and recurring expenses.

CREATE TYPE expense_status AS ENUM (
    'DRAFT',        -- created, not yet submitted
    'SUBMITTED',    -- submitted for approval
    'APPROVED',     -- approved, ready to pay
    'PAID',         -- payment has been made
    'REJECTED',     -- rejected by approver
    'CANCELLED'     -- cancelled before payment
    );

CREATE TYPE expense_category AS ENUM (
    'FACILITIES',           -- rent, utilities, maintenance
    'STAFF_COMPENSATION',   -- salaries, honoraria, stipends
    'MINISTRY_SUPPLIES',    -- books, materials, curriculum
    'TECHNOLOGY',           -- software, hardware, streaming
    'WORSHIP',              -- instruments, music licenses, sound
    'OUTREACH',             -- community programs, evangelism
    'MISSIONS',             -- missionary support, trips
    'ADMINISTRATION',       -- office supplies, printing, postage
    'FOOD_AND_HOSPITALITY', -- meals, events, fellowship
    'TRANSPORTATION',       -- fuel, vehicle maintenance, travel
    'BENEVOLENCE',          -- member aid, emergency assistance
    'EDUCATION',            -- training, conferences, books
    'MARKETING',            -- flyers, social media, signage
    'INSURANCE',            -- church liability, property insurance
    'OTHER'
    );

CREATE TABLE IF NOT EXISTS expenses (
                                        id                  UUID             PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Description
                                        title               VARCHAR(300)     NOT NULL,
                                        description         TEXT,
                                        category            expense_category NOT NULL DEFAULT 'OTHER',

    -- Amount
                                        amount              NUMERIC(12,2)    NOT NULL CHECK (amount > 0),
                                        currency            VARCHAR(3)       NOT NULL DEFAULT 'USD',

    -- Vendor / payee
                                        vendor_name         VARCHAR(255),
                                        vendor_reference    VARCHAR(100),    -- invoice number, receipt number

    -- Dates
                                        expense_date        DATE             NOT NULL,
                                        due_date            DATE,            -- when payment is due
                                        paid_date           DATE,            -- when actually paid

    -- Payment
                                        payment_method      payment_method,  -- set when PAID
                                        payment_reference   VARCHAR(100),    -- cheque number, transfer ref

    -- Workflow
                                        status              expense_status   NOT NULL DEFAULT 'DRAFT',
                                        submitted_by        VARCHAR(50)      NOT NULL,  -- membership number
                                        submitted_at        TIMESTAMP,
                                        approved_by         VARCHAR(50),     -- membership number of approver
                                        approved_at         TIMESTAMP,
                                        rejection_reason    TEXT,

    -- Recurring flag
                                        is_recurring        BOOLEAN          NOT NULL DEFAULT FALSE,
                                        recurrence_note     VARCHAR(255),    -- e.g. "Monthly — every 1st"

    -- Receipt / attachment reference (URL or file path)
                                        receipt_url         VARCHAR(500),

                                        notes               TEXT,

    -- Audit
                                        recorded_by         VARCHAR(50)      NOT NULL,
                                        created_at          TIMESTAMP        NOT NULL DEFAULT NOW(),
                                        updated_at          TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_category    ON expenses(category);
CREATE INDEX idx_expenses_status      ON expenses(status);
CREATE INDEX idx_expenses_date        ON expenses(expense_date);
CREATE INDEX idx_expenses_submitted   ON expenses(submitted_by);
CREATE INDEX idx_expenses_approved    ON expenses(approved_by);
CREATE INDEX idx_expenses_due         ON expenses(due_date);