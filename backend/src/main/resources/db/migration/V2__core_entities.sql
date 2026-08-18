-- ============================================
-- LPMS Core Entities
-- Version: 2.0.0
-- Description: Core entities for Legal Practice Management System
-- ============================================

-- ============================================
-- LAW FIRMS
-- ============================================
CREATE TABLE IF NOT EXISTS law_firms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    logo_url VARCHAR(500),
    website VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    subscription_tier VARCHAR(50) DEFAULT 'STANDARD',
    max_users INT DEFAULT 10,
    max_storage_gb INT DEFAULT 100,
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    INDEX idx_law_firms_name (name),
    INDEX idx_law_firms_status (status)
);

-- ============================================
-- BRANCHES
-- ============================================
CREATE TABLE IF NOT EXISTS branches (
    id VARCHAR(36) PRIMARY KEY,
    law_firm_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    is_headquarters BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    INDEX idx_branches_firm (law_firm_id),
    INDEX idx_branches_code (code),
    UNIQUE KEY uk_branches_firm_code (law_firm_id, code)
);

-- ============================================
-- DEPARTMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(36) PRIMARY KEY,
    branch_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    head_user_id VARCHAR(36),
    parent_department_id VARCHAR(36),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_department_id) REFERENCES departments(id) ON DELETE SET NULL,
    INDEX idx_departments_branch (branch_id),
    UNIQUE KEY uk_departments_branch_code (branch_id, code)
);

-- ============================================
-- USERS
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    law_firm_id VARCHAR(36) NOT NULL,
    branch_id VARCHAR(36),
    department_id VARCHAR(36),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    preferred_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    date_of_birth DATE,
    gender VARCHAR(20),
    nationality VARCHAR(100),
    identification_type VARCHAR(50),
    identification_number VARCHAR(100),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_token_expires_at TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expires_at TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    settings JSON,
    preferences JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_law_firm (law_firm_id),
    INDEX idx_users_branch (branch_id),
    INDEX idx_users_status (status)
);

-- ============================================
-- ROLES
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    UNIQUE KEY uk_roles_name (name)
);

-- ============================================
-- USER ROLES
-- ============================================
CREATE TABLE IF NOT EXISTS user_roles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    branch_id VARCHAR(36),
    assigned_by VARCHAR(36),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_roles_user_role (user_id, role_id, branch_id),
    INDEX idx_user_roles_user (user_id),
    INDEX idx_user_roles_role (role_id)
);

-- ============================================
-- PERMISSIONS
-- ============================================
CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    UNIQUE KEY uk_permissions_code (code)
);

-- ============================================
-- ROLE PERMISSIONS
-- ============================================
CREATE TABLE IF NOT EXISTS role_permissions (
    id VARCHAR(36) PRIMARY KEY,
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permissions_role_perm (role_id, permission_id)
);

-- ============================================
-- LAWYERS
-- ============================================
CREATE TABLE IF NOT EXISTS lawyers (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    bar_number VARCHAR(100),
    license_number VARCHAR(100),
    license_issue_date DATE,
    license_expiry_date DATE,
    license_issuing_authority VARCHAR(255),
    specializations JSON,
    languages JSON,
    bio TEXT,
    years_of_experience INT,
    hourly_rate DECIMAL(10,2) DEFAULT 0,
    availability_status VARCHAR(50) DEFAULT 'AVAILABLE',
    case_load_limit INT DEFAULT 50,
    profile_complete BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) DEFAULT 0,
    total_cases INT DEFAULT 0,
    closed_cases INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    UNIQUE KEY uk_lawyers_user (user_id),
    INDEX idx_lawyers_firm (law_firm_id),
    INDEX idx_lawyers_bar (bar_number)
);

-- ============================================
-- PARALEGALS
-- ============================================
CREATE TABLE IF NOT EXISTS paralegals (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    certification VARCHAR(100),
    specializations JSON,
    languages JSON,
    bio TEXT,
    years_of_experience INT,
    hourly_rate DECIMAL(10,2) DEFAULT 0,
    assigned_lawyer_id VARCHAR(36),
    availability_status VARCHAR(50) DEFAULT 'AVAILABLE',
    profile_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_lawyer_id) REFERENCES lawyers(id) ON DELETE SET NULL,
    UNIQUE KEY uk_paralegals_user (user_id),
    INDEX idx_paralegals_firm (law_firm_id)
);

-- ============================================
-- STAFF
-- ============================================
CREATE TABLE IF NOT EXISTS staff (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    job_title VARCHAR(100),
    employee_id VARCHAR(50),
    hire_date DATE,
    employment_type VARCHAR(50),
    department_id VARCHAR(36),
    supervisor_id VARCHAR(36),
    salary DECIMAL(12,2),
    profile_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (supervisor_id) REFERENCES staff(id) ON DELETE SET NULL,
    UNIQUE KEY uk_staff_user (user_id),
    INDEX idx_staff_firm (law_firm_id)
);

-- ============================================
-- CLIENTS
-- ============================================
CREATE TABLE IF NOT EXISTS clients (
    id VARCHAR(36) PRIMARY KEY,
    client_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    client_type VARCHAR(50) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(255),
    registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    nationality VARCHAR(100),
    identification_type VARCHAR(50),
    identification_number VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20),
    website VARCHAR(255),
    industry VARCHAR(100),
    company_size VARCHAR(50),
    annual_revenue DECIMAL(15,2),
    risk_level VARCHAR(50) DEFAULT 'LOW',
    vip BOOLEAN DEFAULT FALSE,
    credit_limit DECIMAL(12,2) DEFAULT 0,
    billing_model VARCHAR(50) DEFAULT 'HOURLY',
    preferred_payment_method VARCHAR(50),
    billing_address_same_as_postal BOOLEAN DEFAULT TRUE,
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_country VARCHAR(100),
    billing_postal_code VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    referred_by VARCHAR(255),
    source VARCHAR(100),
    notes TEXT,
    preferences JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    UNIQUE KEY uk_clients_number (client_number),
    INDEX idx_clients_firm (law_firm_id),
    INDEX idx_clients_type (client_type),
    INDEX idx_clients_status (status),
    INDEX idx_clients_company (company_name)
);

-- ============================================
-- CLIENT CONTACTS
-- ============================================
CREATE TABLE IF NOT EXISTS client_contacts (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    job_title VARCHAR(100),
    department VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    can_receive_invoices BOOLEAN DEFAULT FALSE,
    can_receive_notifications BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    INDEX idx_client_contacts_client (client_id)
);

-- ============================================
-- CLIENT ADDRESSES
-- ============================================
CREATE TABLE IF NOT EXISTS client_addresses (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL,
    address_type VARCHAR(50) NOT NULL,
    label VARCHAR(100),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    INDEX idx_client_addresses_client (client_id)
);