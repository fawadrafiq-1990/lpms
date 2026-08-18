-- ============================================
-- V10: Add denormalized columns referenced by Hearing/Task entities
-- ============================================
-- The Hearing and Task JPA entities map convenience (denormalized) columns
-- (case_number, court_name, assigned_to_name, ...) that the original V3/V4
-- schemas never created. Without them, every Hearing/Task query fails with
-- "Unknown column" (HTTP 500), which breaks the dashboard's upcoming-hearings
-- and pending-tasks cards. Add them as nullable so existing rows are valid.

-- Hearings: missing denormalized columns
ALTER TABLE hearings
    ADD COLUMN case_number VARCHAR(50) NULL AFTER case_id,
    ADD COLUMN case_title VARCHAR(500) NULL AFTER case_number,
    ADD COLUMN court_name VARCHAR(255) NULL AFTER court_id,
    ADD COLUMN courtroom_name VARCHAR(100) NULL AFTER courtroom_id,
    ADD COLUMN judge_name VARCHAR(255) NULL AFTER judge_id;

-- Tasks: missing denormalized columns
ALTER TABLE tasks
    ADD COLUMN case_number VARCHAR(50) NULL AFTER case_id,
    ADD COLUMN assigned_to_name VARCHAR(255) NULL AFTER assigned_to;
