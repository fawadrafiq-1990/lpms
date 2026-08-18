-- ============================================
-- V8: Seed lawyer profiles + sample clients and cases
-- ============================================
-- Creates lawyer profiles for existing LAWYER-role users (so the assign-lawyer
-- dropdown and case_lawyers FK have valid targets), plus a few sample clients
-- and cases so the dashboard and lists are not empty after the frontend wiring.

-- Lawyer profile for user-003 (lawyer@lawfirm.com) — lawyers.id is referenced
-- by cases.lead_lawyer_id and case_lawyers.lawyer_id.
INSERT INTO lawyers (id, user_id, law_firm_id, bar_number, license_number,
                     hourly_rate, availability_status, case_load_limit,
                     profile_complete, rating, total_cases, closed_cases)
VALUES ('lawyer-003', 'user-003', 'firm-001', 'BAR-AE-1001', 'LIC-AE-5001',
        450.00, 'AVAILABLE', 50, TRUE, 4.5, 0, 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Lawyer profile for user-001 (admin@lawfirm.com) so an admin can also be a
-- lead lawyer / assigned when testing.
INSERT INTO lawyers (id, user_id, law_firm_id, bar_number, license_number,
                     hourly_rate, availability_status, case_load_limit,
                     profile_complete, rating, total_cases, closed_cases)
VALUES ('lawyer-001', 'user-001', 'firm-001', 'BAR-AE-2001', 'LIC-AE-6001',
        600.00, 'AVAILABLE', 50, TRUE, 4.8, 0, 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- Sample clients (firm-001)
-- ============================================
INSERT INTO clients (id, client_number, law_firm_id, client_type, first_name, last_name,
                     company_name, email, phone, nationality, risk_level, vip, status,
                     city, country, created_at, updated_at, version)
VALUES
('client-001', 'CL-0001', 'firm-001', 'INDIVIDUAL', 'Ahmed', 'Al Mansoori',
 NULL, 'ahmed.almansoori@example.com', '+971501234567', 'Emirati', 'LOW', FALSE, 'ACTIVE',
 'Abu Dhabi', 'UAE', NOW(), NOW(), 0),
('client-002', 'CL-0002', 'firm-001', 'COMPANY', NULL, NULL,
 'Al Mana Properties LLC', 'legal@almanaprops.com', '+971502345678', 'UAE', 'MEDIUM', TRUE, 'ACTIVE',
 'Dubai', 'UAE', NOW(), NOW(), 0),
('client-003', 'CL-0003', 'firm-001', 'INDIVIDUAL', 'Sarah', 'Johnson',
 NULL, 'sarah.johnson@example.com', '+971503456789', 'British', 'LOW', FALSE, 'ACTIVE',
 'Abu Dhabi', 'UAE', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- Sample cases (firm-001)
-- ============================================
INSERT INTO cases (id, case_number, matter_number, law_firm_id, client_id, client_name,
                   case_type, case_status, case_priority, practice_area, title, description,
                   case_value, confidentiality_level, risk_level, opening_date,
                   lead_lawyer_id, lead_lawyer_name, billing_model, total_billed,
                   total_paid, total_outstanding, created_at, updated_at, version)
VALUES
('case-001', 'CASE-10021', 'M-001', 'firm-001', 'client-001', 'Ahmed Al Mansoori',
 'LITIGATION', 'IN_PROGRESS', 'HIGH', 'Litigation', 'Smith vs. Johnson Corp',
 'Civil litigation regarding breach of contract.',
 250000.00, 'CONFIDENTIAL', 'MEDIUM', CURDATE(),
 'lawyer-003', 'Demo Lawyer', 'HOURLY', 12000.00, 6000.00, 6000.00, NOW(), NOW(), 0),
('case-002', 'CASE-10022', 'M-002', 'firm-001', 'client-002', 'Al Mana Properties LLC',
 'CORPORATE', 'OPEN', 'MEDIUM', 'Real Estate', 'Al Mana Properties Acquisition',
 'Legal review and structuring of a commercial property acquisition.',
 5000000.00, 'STRICTLY_CONFIDENTIAL', 'LOW', CURDATE(),
 'lawyer-001', 'Demo Admin', 'FIXED_FEE', 35000.00, 35000.00, 0.00, NOW(), NOW(), 0),
('case-003', 'CASE-10023', 'M-003', 'firm-001', 'client-003', 'Sarah Johnson',
 'FAMILY', 'NEW', 'MEDIUM', 'Family', 'Johnson Family Estate Planning',
 'Drafting of will and estate planning documents.',
 8000.00, 'CONFIDENTIAL', 'LOW', CURDATE(),
 'lawyer-003', 'Demo Lawyer', 'HOURLY', 0.00, 0.00, 0.00, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Assign the lead lawyer explicitly via case_lawyers for case-001
INSERT INTO case_lawyers (id, case_id, lawyer_id, role, is_primary, billing_rate,
                          hours_allocated, hours_logged, assigned_at, assigned_by)
VALUES ('cl-001', 'case-001', 'lawyer-003', 'LEAD', TRUE, 450.00, 40.0, 12.5, NOW(), 'user-001')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
