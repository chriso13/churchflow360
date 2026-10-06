-- V1__create_tithes.sql
-- Tithe records — always linked to a registered member.
-- A tithe is a covenant giving (10% of income) from a specific member.

-- Shared payment method enum used across all three giving types
CREATE TYPE payment_method AS ENUM (
    'CASH',
    'CHECK',
    'BANK_TRANSFER',
    'CREDIT_CARD',
    'DEBIT_CARD',
    'MOBILE_PAYMENT',
    'ONLINE'
    );

-- Tithe period — which month/year the tithe covers
CREATE TYPE tithe_period AS ENUM (
    'JANUARY', 'FEBRUARY', 'MARCH',     'APRIL',
    'MAY',     'JUNE',     'JULY',      'AUGUST',
    'SEPTEMBER','OCTOBER', 'NOVEMBER',  'DECEMBER'
    );

CREATE TABLE IF NOT EXISTS tithes (
                                      id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Member link — tithes are ALWAYS tied to a registered member
                                      membership_number   VARCHAR(50)    NOT NULL,
                                      member_name         VARCHAR(255)   NOT NULL,

    -- Amount
                                      amount              NUMERIC(12,2)  NOT NULL CHECK (amount > 0),
                                      currency            VARCHAR(3)     NOT NULL DEFAULT 'USD',

    -- When and how
                                      payment_date        DATE           NOT NULL,
                                      payment_method      payment_method NOT NULL,
                                      reference_number    VARCHAR(100),   -- check number, transaction ID, etc.

    -- Which period this tithe covers
                                      tithe_month         tithe_period   NOT NULL,
                                      tithe_year          INTEGER        NOT NULL CHECK (tithe_year >= 2000),

    -- Optional notes (envelope number, special instruction)
                                      notes               TEXT,

    -- Audit
                                      recorded_by         VARCHAR(50)    NOT NULL,  -- membership number of recorder
                                      created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
                                      updated_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tithes_member   ON tithes(membership_number);
CREATE INDEX idx_tithes_date     ON tithes(payment_date);
CREATE INDEX idx_tithes_period   ON tithes(tithe_year, tithe_month);
CREATE INDEX idx_tithes_method   ON tithes(payment_method);

-- Prevent duplicate tithe for same member + month + year
CREATE UNIQUE INDEX uq_tithe_member_period
    ON tithes(membership_number, tithe_month, tithe_year);