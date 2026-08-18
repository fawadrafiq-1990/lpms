-- ============================================
-- LPMS Database Schema
-- Version: 1.0.0
-- Description: Initial schema for Legal Practice Management System
-- ============================================

-- ============================================
-- ENUMS
-- ============================================

CREATE TABLE IF NOT EXISTS enum_user_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_role (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_case_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_case_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_case_priority (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_task_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_hearing_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_invoice_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_document_category (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_client_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_payment_method (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_notification_channel (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_case_party_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_conflict_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_expense_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_expense_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_contract_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_deadline_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_evidence_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_evidence_status (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_court_type (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_practice_area (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enum_billing_model (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Insert Enum Values
-- ============================================

-- User Status
INSERT INTO enum_user_status (id, name, description) VALUES
    (UUID(), 'ACTIVE', 'User is active'),
    (UUID(), 'INACTIVE', 'User is inactive'),
    (UUID(), 'SUSPENDED', 'User is suspended'),
    (UUID(), 'PENDING', 'User is pending verification'),
    (UUID(), 'LOCKED', 'User account is locked');

-- Roles
INSERT INTO enum_role (id, name, description) VALUES
    (UUID(), 'SUPER_ADMIN', 'Super Administrator with full system access'),
    (UUID(), 'FIRM_ADMIN', 'Law firm administrator'),
    (UUID(), 'MANAGING_PARTNER', 'Managing Partner'),
    (UUID(), 'SENIOR_LAWYER', 'Senior Lawyer'),
    (UUID(), 'LAWYER', 'Lawyer'),
    (UUID(), 'PARALEGAL', 'Paralegal'),
    (UUID(), 'LEGAL_ASSISTANT', 'Legal Assistant'),
    (UUID(), 'ACCOUNTANT', 'Accountant'),
    (UUID(), 'RECEPTIONIST', 'Receptionist'),
    (UUID(), 'CLIENT', 'Client');

-- Case Status
INSERT INTO enum_case_status (id, name, description, display_order) VALUES
    (UUID(), 'NEW', 'New case created', 1),
    (UUID(), 'CONFLICT_CHECK', 'Conflict check in progress', 2),
    (UUID(), 'OPEN', 'Case is open', 3),
    (UUID(), 'PREPARATION', 'Case preparation in progress', 4),
    (UUID(), 'FILED', 'Case filed with court', 5),
    (UUID(), 'IN_PROGRESS', 'Case is in progress', 6),
    (UUID(), 'HEARING', 'Case has upcoming hearings', 7),
    (UUID(), 'JUDGMENT', 'Awaiting judgment', 8),
    (UUID(), 'APPEAL', 'Case is under appeal', 9),
    (UUID(), 'SETTLEMENT', 'Settlement negotiations', 10),
    (UUID(), 'CLOSED', 'Case is closed', 11),
    (UUID(), 'ARCHIVED', 'Case is archived', 12);

-- Case Priority
INSERT INTO enum_case_priority (id, name, description) VALUES
    (UUID(), 'LOW', 'Low priority'),
    (UUID(), 'MEDIUM', 'Medium priority'),
    (UUID(), 'HIGH', 'High priority'),
    (UUID(), 'URGENT', 'Urgent priority'),
    (UUID(), 'CRITICAL', 'Critical priority');

-- Task Status
INSERT INTO enum_task_status (id, name, description) VALUES
    (UUID(), 'TODO', 'Task to be done'),
    (UUID(), 'IN_PROGRESS', 'Task is in progress'),
    (UUID(), 'BLOCKED', 'Task is blocked'),
    (UUID(), 'REVIEW', 'Task is under review'),
    (UUID(), 'COMPLETED', 'Task is completed'),
    (UUID(), 'CANCELLED', 'Task is cancelled');

-- Hearing Type
INSERT INTO enum_hearing_type (id, name, description) VALUES
    (UUID(), 'INITIAL', 'Initial hearing'),
    (UUID(), 'ARGUMENT', 'Argument hearing'),
    (UUID(), 'EVIDENCE', 'Evidence hearing'),
    (UUID(), 'WITNESS', 'Witness hearing'),
    (UUID(), 'CROSS_EXAMINATION', 'Cross-examination'),
    (UUID(), 'FINAL_ARGUMENT', 'Final argument'),
    (UUID(), 'JUDGMENT', 'Judgment hearing'),
    (UUID(), 'APPEAL', 'Appeal hearing'),
    (UUID(), 'OTHER', 'Other hearing');

-- Invoice Status
INSERT INTO enum_invoice_status (id, name, description) VALUES
    (UUID(), 'DRAFT', 'Invoice is in draft'),
    (UUID(), 'ISSUED', 'Invoice is issued'),
    (UUID(), 'PARTIALLY_PAID', 'Invoice is partially paid'),
    (UUID(), 'PAID', 'Invoice is paid'),
    (UUID(), 'OVERDUE', 'Invoice is overdue'),
    (UUID(), 'CANCELLED', 'Invoice is cancelled');

-- Document Category
INSERT INTO enum_document_category (id, name, description) VALUES
    (UUID(), 'CONTRACT', 'Contract documents'),
    (UUID(), 'COURT_ORDER', 'Court orders'),
    (UUID(), 'PLEADING', 'Pleadings'),
    (UUID(), 'EVIDENCE', 'Evidence documents'),
    (UUID(), 'IDENTITY', 'Identity documents'),
    (UUID(), 'LEGAL_NOTICE', 'Legal notices'),
    (UUID(), 'AGREEMENT', 'Agreements'),
    (UUID(), 'EMAIL', 'Email correspondence'),
    (UUID(), 'REPORT', 'Reports'),
    (UUID(), 'JUDGMENT', 'Court judgments'),
    (UUID(), 'CORRESPONDENCE', 'General correspondence'),
    (UUID(), 'OTHER', 'Other documents');

-- Client Type
INSERT INTO enum_client_type (id, name, description) VALUES
    (UUID(), 'INDIVIDUAL', 'Individual client'),
    (UUID(), 'COMPANY', 'Corporate client'),
    (UUID(), 'GOVERNMENT', 'Government entity'),
    (UUID(), 'ORGANIZATION', 'Non-profit organization');

-- Payment Method
INSERT INTO enum_payment_method (id, name, description) VALUES
    (UUID(), 'CASH', 'Cash payment'),
    (UUID(), 'BANK_TRANSFER', 'Bank transfer'),
    (UUID(), 'CARD', 'Credit/Debit card'),
    (UUID(), 'ONLINE', 'Online payment'),
    (UUID(), 'CHECK', 'Check payment'),
    (UUID(), 'OTHER', 'Other payment method');

-- Notification Channel
INSERT INTO enum_notification_channel (id, name, description) VALUES
    (UUID(), 'IN_APP', 'In-app notification'),
    (UUID(), 'EMAIL', 'Email notification'),
    (UUID(), 'PUSH', 'Push notification'),
    (UUID(), 'SMS', 'SMS notification');

-- Case Party Type
INSERT INTO enum_case_party_type (id, name, description) VALUES
    (UUID(), 'CLIENT', 'Main client'),
    (UUID(), 'CO_CLIENT', 'Co-client'),
    (UUID(), 'PLAINTIFF', 'Plaintiff'),
    (UUID(), 'DEFENDANT', 'Defendant'),
    (UUID(), 'RESPONDENT', 'Respondent'),
    (UUID(), 'APPLICANT', 'Applicant'),
    (UUID(), 'PETITIONER', 'Petitioner'),
    (UUID(), 'WITNESS', 'Witness'),
    (UUID(), 'EXPERT', 'Expert witness'),
    (UUID(), 'OPPOSING_COUNSEL', 'Opposing counsel'),
    (UUID(), 'OTHER', 'Other party');

-- Conflict Status
INSERT INTO enum_conflict_status (id, name, description) VALUES
    (UUID(), 'NO_CONFLICT', 'No conflict found'),
    (UUID(), 'POSSIBLE_CONFLICT', 'Possible conflict requires review'),
    (UUID(), 'CONFLICT_FOUND', 'Conflict found - cannot proceed');

-- Expense Type
INSERT INTO enum_expense_type (id, name, description) VALUES
    (UUID(), 'COURT_FEES', 'Court filing fees'),
    (UUID(), 'TRAVEL', 'Travel expenses'),
    (UUID(), 'COURIER', 'Courier and shipping'),
    (UUID(), 'PRINTING', 'Printing and copying'),
    (UUID(), 'TRANSLATION', 'Translation services'),
    (UUID(), 'EXPERT_FEES', 'Expert witness fees'),
    (UUID(), 'RESEARCH', 'Legal research costs'),
    (UUID(), 'OTHER', 'Other expenses');

-- Expense Status
INSERT INTO enum_expense_status (id, name, description) VALUES
    (UUID(), 'PENDING', 'Expense pending approval'),
    (UUID(), 'APPROVED', 'Expense approved'),
    (UUID(), 'REJECTED', 'Expense rejected'),
    (UUID(), 'REIMBURSED', 'Expense reimbursed');

-- Contract Status
INSERT INTO enum_contract_status (id, name, description) VALUES
    (UUID(), 'DRAFT', 'Contract in draft'),
    (UUID(), 'PENDING_REVIEW', 'Pending review'),
    (UUID(), 'PENDING_SIGNATURE', 'Pending signature'),
    (UUID(), 'ACTIVE', 'Contract is active'),
    (UUID(), 'EXPIRED', 'Contract expired'),
    (UUID(), 'TERMINATED', 'Contract terminated'),
    (UUID(), 'RENEWED', 'Contract renewed');

-- Deadline Type
INSERT INTO enum_deadline_type (id, name, description) VALUES
    (UUID(), 'COURT_DEADLINE', 'Court-imposed deadline'),
    (UUID(), 'FILING_DEADLINE', 'Filing deadline'),
    (UUID(), 'INTERNAL_DEADLINE', 'Internal deadline'),
    (UUID(), 'CLIENT_DEADLINE', 'Client-defined deadline'),
    (UUID(), 'CONTRACT_DEADLINE', 'Contract deadline'),
    (UUID(), 'APPEAL_DEADLINE', 'Appeal deadline');

-- Evidence Type
INSERT INTO enum_evidence_type (id, name, description) VALUES
    (UUID(), 'DOCUMENT', 'Documentary evidence'),
    (UUID(), 'PHYSICAL', 'Physical evidence'),
    (UUID(), 'TESTIMONIAL', 'Testimonial evidence'),
    (UUID(), 'DIGITAL', 'Digital/electronic evidence'),
    (UUID(), 'EXHIBITS', 'Court exhibits'),
    (UUID(), 'OTHER', 'Other evidence');

-- Evidence Status
INSERT INTO enum_evidence_status (id, name, description) VALUES
    (UUID(), 'COLLECTED', 'Evidence collected'),
    (UUID(), 'ANALYZED', 'Evidence analyzed'),
    (UUID(), 'SUBMITTED', 'Submitted to court'),
    (UUID(), 'ADMITTED', 'Admitted by court'),
    (UUID(), 'REJECTED', 'Rejected by court'),
    (UUID(), 'RETURNED', 'Returned to owner');

-- Court Type
INSERT INTO enum_court_type (id, name, description) VALUES
    (UUID(), 'SUPREME', 'Supreme Court'),
    (UUID(), 'COURT_OF_APPEAL', 'Court of Appeal'),
    (UUID(), 'HIGH', 'High Court'),
    (UUID(), 'DISTRICT', 'District Court'),
    (UUID(), 'MAGISTRATE', 'Magistrate Court'),
    (UUID(), 'FAMILY', 'Family Court'),
    (UUID(), 'COMMERCIAL', 'Commercial Court'),
    (UUID(), 'LABOR', 'Labor Court'),
    (UUID(), 'ADMINISTRATIVE', 'Administrative Court'),
    (UUID(), 'OTHER', 'Other court');

-- Practice Area
INSERT INTO enum_practice_area (id, name, description) VALUES
    (UUID(), 'CORPORATE', 'Corporate Law'),
    (UUID(), 'LITIGATION', 'Litigation'),
    (UUID(), 'CRIMINAL', 'Criminal Law'),
    (UUID(), 'FAMILY', 'Family Law'),
    (UUID(), 'REAL_ESTATE', 'Real Estate Law'),
    (UUID(), 'INTELLECTUAL_PROPERTY', 'Intellectual Property'),
    (UUID(), 'LABOR', 'Labor Law'),
    (UUID(), 'TAX', 'Tax Law'),
    (UUID(), 'BANKRUPTCY', 'Bankruptcy Law'),
    (UUID(), 'IMMIGRATION', 'Immigration Law'),
    (UUID(), 'ENVIRONMENTAL', 'Environmental Law'),
    (UUID(), 'HEALTHCARE', 'Healthcare Law'),
    (UUID(), 'BANKING', 'Banking and Finance'),
    (UUID(), 'INSURANCE', 'Insurance Law'),
    (UUID(), 'OTHER', 'Other practice area');

-- Billing Model
INSERT INTO enum_billing_model (id, name, description) VALUES
    (UUID(), 'HOURLY', 'Hourly billing'),
    (UUID(), 'FIXED_FEE', 'Fixed fee billing'),
    (UUID(), 'RETAINER', 'Retainer billing'),
    (UUID(), 'CONTINGENCY', 'Contingency billing'),
    (UUID(), 'MIXED', 'Mixed billing model');

-- Case Type
INSERT INTO enum_case_type (id, name, description) VALUES
    (UUID(), 'CIVIL', 'Civil case'),
    (UUID(), 'CRIMINAL', 'Criminal case'),
    (UUID(), 'COMMERCIAL', 'Commercial dispute'),
    (UUID(), 'FAMILY', 'Family matter'),
    (UUID(), 'LABOR', 'Labor dispute'),
    (UUID(), 'ADMINISTRATIVE', 'Administrative appeal'),
    (UUID(), 'CONSTITUTIONAL', 'Constitutional matter'),
    (UUID(), 'INTELLECTUAL_PROPERTY', 'IP dispute'),
    (UUID(), 'REAL_ESTATE', 'Real estate dispute'),
    (UUID(), 'BANKRUPTCY', 'Bankruptcy proceeding'),
    (UUID(), 'OTHER', 'Other case type');