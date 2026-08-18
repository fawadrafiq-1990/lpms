-- ============================================
-- LPMS Cases, Courts, and Hearings
-- Version: 3.0.0
-- ============================================

-- ============================================
-- COURTS
-- ============================================
CREATE TABLE IF NOT EXISTS courts (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    court_type VARCHAR(50) NOT NULL,
    jurisdiction VARCHAR(255),
    jurisdiction_level VARCHAR(100),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    phone VARCHAR(50),
    fax VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    filing_hours VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    INDEX idx_courts_name (name),
    INDEX idx_courts_type (court_type),
    INDEX idx_courts_country (country),
    INDEX idx_courts_city (city)
);

-- ============================================
-- COURTROOMS
-- ============================================
CREATE TABLE IF NOT EXISTS courtrooms (
    id VARCHAR(36) PRIMARY KEY,
    court_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    room_number VARCHAR(50),
    floor VARCHAR(20),
    capacity INT,
    has_video_conferencing BOOLEAN DEFAULT FALSE,
    has_recording BOOLEAN DEFAULT FALSE,
    facilities JSON,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE,
    INDEX idx_courtrooms_court (court_id)
);

-- ============================================
-- JUDGES
-- ============================================
CREATE TABLE IF NOT EXISTS judges (
    id VARCHAR(36) PRIMARY KEY,
    court_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(50),
    honorific VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(50),
    chamber VARCHAR(100),
    appointment_date DATE,
    seniority_level VARCHAR(50),
    specializations JSON,
    biography TEXT,
    photo_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE,
    INDEX idx_judges_court (court_id),
    INDEX idx_judges_name (first_name, last_name)
);

-- ============================================
-- CASES
-- ============================================
CREATE TABLE IF NOT EXISTS cases (
    id VARCHAR(36) PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL,
    matter_number VARCHAR(50),
    law_firm_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    case_type VARCHAR(50) NOT NULL,
    case_status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    case_priority VARCHAR(50) DEFAULT 'MEDIUM',
    practice_area VARCHAR(100),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    court_id VARCHAR(36),
    judge_id VARCHAR(36),
    opposing_party_id VARCHAR(36),
    filing_number VARCHAR(100),
    filing_date DATE,
    case_value DECIMAL(15,2),
    estimated_duration VARCHAR(100),
    confidentiality_level VARCHAR(50) DEFAULT 'INTERNAL',
    risk_level VARCHAR(50) DEFAULT 'LOW',
    opening_date DATE,
    expected_closing_date DATE,
    actual_closing_date DATE,
    referral_source VARCHAR(255),
    lead_lawyer_id VARCHAR(36),
    billing_model VARCHAR(50) DEFAULT 'HOURLY',
    retainer_amount DECIMAL(12,2) DEFAULT 0,
    retainer_balance DECIMAL(12,2) DEFAULT 0,
    total_billed DECIMAL(15,2) DEFAULT 0,
    total_paid DECIMAL(15,2) DEFAULT 0,
    total_outstanding DECIMAL(15,2) DEFAULT 0,
    settings JSON,
    client_name VARCHAR(255),
    court_name VARCHAR(255),
    lead_lawyer_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE SET NULL,
    FOREIGN KEY (judge_id) REFERENCES judges(id) ON DELETE SET NULL,
    FOREIGN KEY (lead_lawyer_id) REFERENCES lawyers(id) ON DELETE SET NULL,
    UNIQUE KEY uk_cases_number (case_number),
    INDEX idx_cases_firm (law_firm_id),
    INDEX idx_cases_client (client_id),
    INDEX idx_cases_status (case_status),
    INDEX idx_cases_type (case_type),
    INDEX idx_cases_court (court_id),
    INDEX idx_cases_judge (judge_id),
    INDEX idx_cases_priority (case_priority)
);

-- ============================================
-- CASE LAWYERS
-- ============================================
CREATE TABLE IF NOT EXISTS case_lawyers (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    lawyer_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) DEFAULT 'ASSOCIATE',
    is_primary BOOLEAN DEFAULT FALSE,
    billing_rate DECIMAL(10,2),
    hours_allocated DECIMAL(8,2),
    hours_logged DECIMAL(8,2) DEFAULT 0,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (lawyer_id) REFERENCES lawyers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_case_lawyers_case_lawyer (case_id, lawyer_id),
    INDEX idx_case_lawyers_case (case_id),
    INDEX idx_case_lawyers_lawyer (lawyer_id)
);

-- ============================================
-- CASE PARALEGALS
-- ============================================
CREATE TABLE IF NOT EXISTS case_paralegals (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    paralegal_id VARCHAR(36) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (paralegal_id) REFERENCES paralegals(id) ON DELETE CASCADE,
    UNIQUE KEY uk_case_paralegals_case_paralegal (case_id, paralegal_id),
    INDEX idx_case_paralegals_case (case_id),
    INDEX idx_case_paralegals_paralegal (paralegal_id)
);

-- ============================================
-- CASE PARTIES
-- ============================================
CREATE TABLE IF NOT EXISTS case_parties (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    party_type VARCHAR(50) NOT NULL,
    party_name VARCHAR(255) NOT NULL,
    party_role VARCHAR(100),
    is_individual BOOLEAN,
    identification_type VARCHAR(50),
    identification_number VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    represented_by VARCHAR(255),
    counsel_email VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    INDEX idx_case_parties_case (case_id),
    INDEX idx_case_parties_type (party_type)
);

-- ============================================
-- OPPOSING PARTIES
-- ============================================
CREATE TABLE IF NOT EXISTS opposing_parties (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    party_type VARCHAR(50),
    identification_number VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    represented_by VARCHAR(255),
    counsel_name VARCHAR(255),
    counsel_email VARCHAR(255),
    counsel_phone VARCHAR(50),
    counsel_bar_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    INDEX idx_opposing_parties_name (name)
);

-- ============================================
-- OPPOSING PARTY CONTACTS
-- ============================================
CREATE TABLE IF NOT EXISTS opposing_party_contacts (
    id VARCHAR(36) PRIMARY KEY,
    opposing_party_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (opposing_party_id) REFERENCES opposing_parties(id) ON DELETE CASCADE,
    INDEX idx_opposing_contacts_party (opposing_party_id)
);

-- ============================================
-- HEARINGS
-- ============================================
CREATE TABLE IF NOT EXISTS hearings (
    id VARCHAR(36) PRIMARY KEY,
    hearing_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36) NOT NULL,
    court_id VARCHAR(36) NOT NULL,
    courtroom_id VARCHAR(36),
    judge_id VARCHAR(36),
    hearing_type VARCHAR(50) NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    duration_minutes INT DEFAULT 60,
    actual_start_time TIME,
    actual_end_time TIME,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    priority VARCHAR(50) DEFAULT 'NORMAL',
    location VARCHAR(500),
    virtual_meeting_url VARCHAR(500),
    virtual_meeting_id VARCHAR(255),
    agenda TEXT,
    purpose TEXT,
    outcome TEXT,
    notes TEXT,
    internal_notes TEXT,
    reminders_sent JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE RESTRICT,
    FOREIGN KEY (courtroom_id) REFERENCES courtrooms(id) ON DELETE SET NULL,
    FOREIGN KEY (judge_id) REFERENCES judges(id) ON DELETE SET NULL,
    UNIQUE KEY uk_hearings_number (hearing_number),
    INDEX idx_hearings_case (case_id),
    INDEX idx_hearings_court (court_id),
    INDEX idx_hearings_date (scheduled_date),
    INDEX idx_hearings_status (status)
);

-- ============================================
-- HEARING PARTICIPANTS
-- ============================================
CREATE TABLE IF NOT EXISTS hearing_participants (
    id VARCHAR(36) PRIMARY KEY,
    hearing_id VARCHAR(36) NOT NULL,
    participant_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(36),
    name VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(100),
    is_present BOOLEAN DEFAULT FALSE,
    arrived_at TIME,
    departed_at TIME,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (hearing_id) REFERENCES hearings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_hearing_participants_hearing (hearing_id)
);

-- ============================================
-- COURT ORDERS
-- ============================================
CREATE TABLE IF NOT EXISTS court_orders (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    hearing_id VARCHAR(36),
    order_number VARCHAR(100),
    order_date DATE NOT NULL,
    order_type VARCHAR(100),
    title VARCHAR(500),
    description TEXT,
    issued_by VARCHAR(255),
    filed_date DATE,
    due_date DATE,
    compliance_status VARCHAR(50) DEFAULT 'PENDING',
    compliance_date DATE,
    compliance_notes TEXT,
    document_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (hearing_id) REFERENCES hearings(id) ON DELETE SET NULL,
    INDEX idx_court_orders_case (case_id),
    INDEX idx_court_orders_date (order_date)
);

-- ============================================
-- CASE TIMELINE
-- ============================================
CREATE TABLE IF NOT EXISTS case_timeline (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    event_date DATE NOT NULL,
    event_time TIME,
    event_type VARCHAR(100) NOT NULL,
    event_title VARCHAR(500) NOT NULL,
    event_description TEXT,
    event_category VARCHAR(50),
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    INDEX idx_case_timeline_case (case_id),
    INDEX idx_case_timeline_date (event_date)
);