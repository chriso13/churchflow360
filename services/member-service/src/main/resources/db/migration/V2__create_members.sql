-- V2__create_members.sql
-- Main members table

CREATE TYPE member_status  AS ENUM ('ACTIVE', 'INACTIVE', 'VISITOR', 'TRANSFERRED', 'DECEASED');
CREATE TYPE gender         AS ENUM ('MALE', 'FEMALE');
CREATE TYPE marital_status AS ENUM ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED');

CREATE TABLE IF NOT EXISTS members (
                                       id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                       membership_number VARCHAR(50)  UNIQUE,
                                       first_name        VARCHAR(100) NOT NULL,
                                       last_name         VARCHAR(100) NOT NULL,
                                       email             VARCHAR(255) UNIQUE,
                                       phone             VARCHAR(30),
                                       date_of_birth     DATE,
                                       gender            gender,
                                       marital_status    marital_status,
                                       address_line1     VARCHAR(255),
                                       address_line2     VARCHAR(255),
                                       city              VARCHAR(100),
                                       state             VARCHAR(100),
                                       zip_code          VARCHAR(20),
                                       country           VARCHAR(100)  DEFAULT 'United States',
                                       profile_photo     VARCHAR(500),
                                       occupation        VARCHAR(150),
                                       notes             TEXT,
                                       membership_status member_status NOT NULL DEFAULT 'VISITOR',
                                       join_date         DATE,
                                       baptism_date      DATE,
                                       water_baptized    BOOLEAN       DEFAULT FALSE,
                                       created_by        UUID,
                                       created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
                                       updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_members_status   ON members(membership_status);
CREATE INDEX idx_members_name     ON members(last_name, first_name);
CREATE INDEX idx_members_email    ON members(email);
CREATE INDEX idx_members_join_date ON members(join_date);