-- ============================================
-- V13: Seed the demo law firm and demo login users
-- ============================================
-- Why this migration exists:
--   V8 seeds lawyers/cases that reference user ids (user-001, user-003) and
--   law_firm_id 'firm-001', but the users and law_firms rows themselves were
--   only ever inserted by hand into the original database. On a fresh clone,
--   Flyway would therefore fail at V8 with a foreign-key violation
--   (lawyers.user_id -> users.id, lawyers.law_firm_id -> law_firms.id) and,
--   even if it didn't, there would be no account to log in with.
--
-- This migration creates those base rows. It is intentionally idempotent:
--   - it runs BEFORE the FK checks are re-enabled (see spring.flyway.init-sql,
--     which disables foreign-key checks for the whole migration run so V8's
--     dangling references resolve once these rows exist);
--   - it is safe to re-run against a database that already has these rows
--     (ON DUPLICATE KEY / guarded inserts), so it never clobbers a user's
--     real data.
--
-- NOTE: foreign-key checks are disabled for this migration run via
-- spring.flyway.init-sql (SET FOREIGN_KEY_CHECKS=0). That is required because
-- V8 (already applied on existing databases, so it cannot be edited) inserts
-- lawyers/cases before these users exist. On an existing database this
-- migration simply no-ops because the rows are already present.
--
-- Demo credentials (BCrypt, strength 12) — change these before any real use:
--   admin@lawfirm.com   / Admin123!   (Firm Administrator)
--   lawyer@lawfirm.com  / Lawyer123!  (Lawyer)
--   admin@lpms.com      / Admin123!   (Firm Administrator, alternate)

-- ---------------------------------------------------------------------------
-- 1. Demo law firm (referenced as firm-001 by every other seed row)
-- ---------------------------------------------------------------------------
INSERT INTO law_firms (id, name, status)
VALUES ('firm-001', 'Demo Law Firm', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- 2. Demo users (password hashes are freshly generated BCrypt, strength 12)
-- ---------------------------------------------------------------------------
INSERT INTO users (id, law_firm_id, email, password_hash, first_name, last_name)
VALUES
    ('user-001', 'firm-001', 'admin@lpms.com',
     '$2a$12$XjDDFCqM8kQqc/HI8DqLA.eLutnBJHfSio9RNfnp.y2.akrkfv/Km',
     'Admin', 'User'),
    ('user-002', 'firm-001', 'admin@lawfirm.com',
     '$2a$12$XjDDFCqM8kQqc/HI8DqLA.eLutnBJHfSio9RNfnp.y2.akrkfv/Km',
     'Demo', 'Admin'),
    ('user-003', 'firm-001', 'lawyer@lawfirm.com',
     '$2a$12$akvEHdxAusmjv1uVWkXYEO/6EGzko73cbokLQY5JVqkIXh.2CL1vW',
     'Demo', 'Lawyer')
ON DUPLICATE KEY UPDATE
    law_firm_id = VALUES(law_firm_id);

-- ---------------------------------------------------------------------------
-- 3. Role assignments (user_roles has no single-column PK, so guard each
--    insert to stay idempotent)
-- ---------------------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
SELECT 'user-001', r.id
FROM roles r
WHERE r.id = 'ROLE-FIRM-ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur
                  WHERE ur.user_id = 'user-001' AND ur.role_id = 'ROLE-FIRM-ADMIN');

INSERT INTO user_roles (user_id, role_id)
SELECT 'user-002', r.id
FROM roles r
WHERE r.id = 'ROLE-FIRM-ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur
                  WHERE ur.user_id = 'user-002' AND ur.role_id = 'ROLE-FIRM-ADMIN');

INSERT INTO user_roles (user_id, role_id)
SELECT 'user-003', r.id
FROM roles r
WHERE r.id = 'ROLE-LAWYER'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur
                  WHERE ur.user_id = 'user-003' AND ur.role_id = 'ROLE-LAWYER');
