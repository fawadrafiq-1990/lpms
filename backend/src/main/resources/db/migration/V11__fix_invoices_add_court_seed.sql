-- ============================================
-- V11: Fix invoices denormalized columns + seed a court
-- ============================================

-- The Invoice entity maps case_number and client_name (denormalized) but the
-- invoices table (V5) never created them, so every invoice query fails with
-- "Unknown column 'i1_0.case_number'". Add them as nullable.
ALTER TABLE invoices
    ADD COLUMN case_number VARCHAR(50) NULL AFTER case_id,
    ADD COLUMN client_name VARCHAR(255) NULL AFTER client_id;

-- The hearings table requires court_id NOT NULL, but there is no courts
-- endpoint and the courts table is empty. Seed one generic court so the
-- Schedule Hearing form has a valid default to satisfy the constraint.
INSERT INTO courts (id, name, code, court_type, jurisdiction, jurisdiction_level,
                    city, state, country, status, created_at, updated_at, version)
VALUES ('court-001', 'Primary Court', 'CRT-001', 'CIVIL', 'General', 'FIRST_INSTANCE',
        'Abu Dhabi', 'Abu Dhabi', 'UAE', 'ACTIVE', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
