-- ============================================
-- LPMS Billing, Contracts, and Payments
-- Version: 5.0.0
-- ============================================

-- ============================================
-- BILLING RATES
-- ============================================
CREATE TABLE IF NOT EXISTS billing_rates (
    id VARCHAR(36) PRIMARY KEY,
    law_firm_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    role_type VARCHAR(50),
    case_type VARCHAR(50),
    rate_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AED',
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_default BOOLEAN DEFAULT FALSE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_billing_rates_firm (law_firm_id),
    INDEX idx_billing_rates_user (user_id)
);

-- ============================================
-- INVOICES (parent table for invoice_items and payments)
-- ============================================
CREATE TABLE IF NOT EXISTS invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    billing_period_start DATE,
    billing_period_end DATE,
    subtotal DECIMAL(12,2) DEFAULT 0,
    tax_rate DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    discount_rate DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) DEFAULT 0,
    amount_paid DECIMAL(12,2) DEFAULT 0,
    amount_outstanding DECIMAL(12,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'AED',
    status VARCHAR(50) DEFAULT 'DRAFT',
    billing_model VARCHAR(50),
    notes TEXT,
    terms_and_conditions TEXT,
    payment_instructions TEXT,
    paid_at TIMESTAMP,
    issued_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    UNIQUE KEY uk_invoices_number (invoice_number),
    INDEX idx_invoices_firm (law_firm_id),
    INDEX idx_invoices_case (case_id),
    INDEX idx_invoices_client (client_id),
    INDEX idx_invoices_status (status),
    INDEX idx_invoices_date (invoice_date)
);

-- ============================================
-- EXPENSES (parent table for invoice_items)
-- ============================================
CREATE TABLE IF NOT EXISTS expenses (
    id VARCHAR(36) PRIMARY KEY,
    expense_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expense_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AED',
    expense_date DATE NOT NULL,
    vendor_name VARCHAR(255),
    vendor_receipt_number VARCHAR(100),
    receipt_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'PENDING',
    approval_status VARCHAR(50) DEFAULT 'PENDING',
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    is_billable BOOLEAN DEFAULT TRUE,
    is_reimbursed BOOLEAN DEFAULT FALSE,
    reimbursed_at TIMESTAMP,
    reimbursed_by VARCHAR(36),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reimbursed_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_expenses_number (expense_number),
    INDEX idx_expenses_case (case_id),
    INDEX idx_expenses_user (user_id),
    INDEX idx_expenses_status (status)
);

-- ============================================
-- TIME ENTRIES (references invoices)
-- ============================================
CREATE TABLE IF NOT EXISTS time_entries (
    id VARCHAR(36) PRIMARY KEY,
    entry_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    lawyer_id VARCHAR(36),
    task_id VARCHAR(36),
    work_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    duration_minutes INT NOT NULL,
    duration_hours DECIMAL(6,2) GENERATED ALWAYS AS (duration_minutes / 60.0) STORED,
    description TEXT NOT NULL,
    activity_type VARCHAR(100),
    billing_rate DECIMAL(10,2),
    hourly_rate DECIMAL(10,2),
    amount DECIMAL(12,2) GENERATED ALWAYS AS (
        CASE WHEN billing_rate IS NOT NULL THEN billing_rate * (duration_minutes / 60.0)
        ELSE hourly_rate * (duration_minutes / 60.0)
        END
    ) STORED,
    billable BOOLEAN DEFAULT TRUE,
    billed BOOLEAN DEFAULT FALSE,
    invoice_id VARCHAR(36),
    invoice_line_id VARCHAR(36),
    status VARCHAR(50) DEFAULT 'DRAFT',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lawyer_id) REFERENCES lawyers(id) ON DELETE SET NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    UNIQUE KEY uk_time_entries_number (entry_number),
    INDEX idx_time_entries_case (case_id),
    INDEX idx_time_entries_user (user_id),
    INDEX idx_time_entries_date (work_date),
    INDEX idx_time_entries_billed (billed)
);

-- ============================================
-- INVOICE ITEMS (references invoices, time_entries, expenses)
-- ============================================
CREATE TABLE IF NOT EXISTS invoice_items (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    item_number VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    quantity DECIMAL(10,2) DEFAULT 1,
    unit VARCHAR(50) DEFAULT 'HOUR',
    rate DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    time_entry_id VARCHAR(36),
    expense_id VARCHAR(36),
    tax_rate DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    discount_rate DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    sort_order INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (time_entry_id) REFERENCES time_entries(id) ON DELETE SET NULL,
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE SET NULL,
    UNIQUE KEY uk_invoice_items_invoice_item (invoice_id, item_number),
    INDEX idx_invoice_items_invoice (invoice_id)
);

-- ============================================
-- PAYMENTS (references invoices)
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36),
    client_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AED',
    payment_method VARCHAR(50) NOT NULL,
    reference_number VARCHAR(100),
    transaction_id VARCHAR(255),
    payment_date DATE NOT NULL,
    bank_name VARCHAR(100),
    check_number VARCHAR(50),
    check_date DATE,
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    receipt_url VARCHAR(500),
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    UNIQUE KEY uk_payments_number (payment_number),
    INDEX idx_payments_firm (law_firm_id),
    INDEX idx_payments_invoice (invoice_id),
    INDEX idx_payments_client (client_id),
    INDEX idx_payments_date (payment_date)
);

-- ============================================
-- RETAINERS
-- ============================================
CREATE TABLE IF NOT EXISTS retainers (
    id VARCHAR(36) PRIMARY KEY,
    retainer_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    retainer_amount DECIMAL(12,2) NOT NULL,
    current_balance DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AED',
    start_date DATE NOT NULL,
    end_date DATE,
    auto_replenish BOOLEAN DEFAULT FALSE,
    replenishment_threshold DECIMAL(12,2),
    replenishment_amount DECIMAL(12,2),
    billing_model VARCHAR(50) DEFAULT 'RETAINER',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    UNIQUE KEY uk_retainers_number (retainer_number),
    INDEX idx_retainers_firm (law_firm_id),
    INDEX idx_retainers_client (client_id)
);

-- ============================================
-- RETAINER TRANSACTIONS
-- ============================================
CREATE TABLE IF NOT EXISTS retainer_transactions (
    id VARCHAR(36) PRIMARY KEY,
    retainer_id VARCHAR(36) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    description TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    FOREIGN KEY (retainer_id) REFERENCES retainers(id) ON DELETE CASCADE,
    INDEX idx_retainer_transactions_retainer (retainer_id),
    INDEX idx_retainer_transactions_date (transaction_date)
);

-- ============================================
-- CONTRACTS
-- ============================================
CREATE TABLE IF NOT EXISTS contracts (
    id VARCHAR(36) PRIMARY KEY,
    contract_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36),
    case_id VARCHAR(36),
    title VARCHAR(500) NOT NULL,
    contract_type VARCHAR(100),
    status VARCHAR(50) DEFAULT 'DRAFT',
    effective_date DATE NOT NULL,
    expiration_date DATE,
    renewal_date DATE,
    auto_renew BOOLEAN DEFAULT FALSE,
    renewal_term_months INT,
    parties JSON,
    total_value DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'AED',
    payment_terms TEXT,
    special_terms TEXT,
    current_version INT DEFAULT 1,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    signed_at TIMESTAMP,
    signed_by VARCHAR(255),
    signature_url VARCHAR(500),
    document_url VARCHAR(500),
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    UNIQUE KEY uk_contracts_number (contract_number),
    INDEX idx_contracts_firm (law_firm_id),
    INDEX idx_contracts_client (client_id),
    INDEX idx_contracts_status (status),
    INDEX idx_contracts_expiration (expiration_date)
);

-- ============================================
-- CONTRACT VERSIONS
-- ============================================
CREATE TABLE IF NOT EXISTS contract_versions (
    id VARCHAR(36) PRIMARY KEY,
    contract_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    title VARCHAR(500),
    content TEXT,
    change_summary TEXT,
    document_url VARCHAR(500),
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_contract_version_contract_version (contract_id, version_number),
    INDEX idx_contract_versions_contract (contract_id)
);

-- ============================================
-- CONTRACT AMENDMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS contract_amendments (
    id VARCHAR(36) PRIMARY KEY,
    contract_id VARCHAR(36) NOT NULL,
    amendment_number VARCHAR(50) NOT NULL,
    title VARCHAR(500),
    description TEXT,
    effective_date DATE,
    document_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'PENDING',
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_contract_amendments_contract_amendment (contract_id, amendment_number),
    INDEX idx_contract_amendments_contract (contract_id)
);
