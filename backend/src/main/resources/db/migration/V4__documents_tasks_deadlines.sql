-- ============================================
-- LPMS Documents, Tasks, and Deadlines
-- Version: 4.0.0
-- ============================================

-- ============================================
-- DOCUMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS case_documents (
    id VARCHAR(36) PRIMARY KEY,
    document_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    lawyer_id VARCHAR(36),
    folder_id VARCHAR(36),
    category VARCHAR(100) NOT NULL,
    document_type VARCHAR(100),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    checksum VARCHAR(64),
    version INT DEFAULT 1,
    is_latest BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    locked_by VARCHAR(36),
    locked_at TIMESTAMP,
    is_confidential BOOLEAN DEFAULT FALSE,
    confidentiality_level VARCHAR(50) DEFAULT 'INTERNAL',
    access_level VARCHAR(50) DEFAULT 'RESTRICTED',
    tags JSON,
    metadata JSON,
    expiration_date DATE,
    is_expired BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version_lock INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (lawyer_id) REFERENCES lawyers(id) ON DELETE SET NULL,
    UNIQUE KEY uk_documents_number (document_number),
    INDEX idx_documents_case (case_id),
    INDEX idx_documents_client (client_id),
    INDEX idx_documents_category (category),
    INDEX idx_documents_created_by (created_by),
    INDEX idx_documents_created_at (created_at)
);

-- ============================================
-- DOCUMENT VERSIONS
-- ============================================
CREATE TABLE IF NOT EXISTS document_versions (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    version_number INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    checksum VARCHAR(64),
    change_description TEXT,
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    FOREIGN KEY (document_id) REFERENCES case_documents(id) ON DELETE CASCADE,
    UNIQUE KEY uk_doc_version_doc_version (document_id, version_number),
    INDEX idx_doc_versions_document (document_id)
);

-- ============================================
-- DOCUMENT FOLDERS
-- ============================================
CREATE TABLE IF NOT EXISTS document_folders (
    id VARCHAR(36) PRIMARY KEY,
    parent_folder_id VARCHAR(36),
    case_id VARCHAR(36),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(20),
    is_shared BOOLEAN DEFAULT FALSE,
    shared_with JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (parent_folder_id) REFERENCES document_folders(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    INDEX idx_doc_folders_parent (parent_folder_id),
    INDEX idx_doc_folders_case (case_id)
);

-- ============================================
-- DOCUMENT PERMISSIONS
-- ============================================
CREATE TABLE IF NOT EXISTS document_permissions (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    role_id VARCHAR(36),
    permission_type VARCHAR(50) NOT NULL,
    granted_by VARCHAR(36),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES case_documents(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_doc_permissions_document (document_id),
    INDEX idx_doc_permissions_user (user_id)
);

-- ============================================
-- EVIDENCE
-- ============================================
CREATE TABLE IF NOT EXISTS evidence (
    id VARCHAR(36) PRIMARY KEY,
    evidence_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36) NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    source VARCHAR(255),
    collected_date DATE,
    collected_by VARCHAR(36),
    location VARCHAR(255),
    current_custodian VARCHAR(36),
    status VARCHAR(50) DEFAULT 'COLLECTED',
    chain_of_custody JSON,
    exhibits JSON,
    analysis_results TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (collected_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (current_custodian) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_evidence_number (evidence_number),
    INDEX idx_evidence_case (case_id),
    INDEX idx_evidence_type (evidence_type),
    INDEX idx_evidence_status (status)
);

-- ============================================
-- EVIDENCE FILES
-- ============================================
CREATE TABLE IF NOT EXISTS evidence_files (
    id VARCHAR(36) PRIMARY KEY,
    evidence_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    checksum VARCHAR(64),
    description TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(36),
    FOREIGN KEY (evidence_id) REFERENCES evidence(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_evidence_files_evidence (evidence_id)
);

-- ============================================
-- TASKS
-- ============================================
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(36) PRIMARY KEY,
    task_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    task_type VARCHAR(100),
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'TODO',
    assigned_to VARCHAR(36) NOT NULL,
    assigned_by VARCHAR(36),
    due_date DATE,
    due_time TIME,
    estimated_hours DECIMAL(6,2),
    actual_hours DECIMAL(6,2) DEFAULT 0,
    progress INT DEFAULT 0,
    billable BOOLEAN DEFAULT TRUE,
    billing_rate DECIMAL(10,2),
    billing_amount DECIMAL(10,2) DEFAULT 0,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern JSON,
    parent_task_id VARCHAR(36),
    depends_on JSON,
    tags JSON,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    UNIQUE KEY uk_tasks_number (task_number),
    INDEX idx_tasks_case (case_id),
    INDEX idx_tasks_assigned (assigned_to),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_due_date (due_date),
    INDEX idx_tasks_priority (priority)
);

-- ============================================
-- TASK COMMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS task_comments (
    id VARCHAR(36) PRIMARY KEY,
    task_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT TRUE,
    mentioned_users JSON,
    parent_comment_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES task_comments(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_task_comments_task (task_id)
);

-- ============================================
-- TASK DEPENDENCIES
-- ============================================
CREATE TABLE IF NOT EXISTS task_dependencies (
    id VARCHAR(36) PRIMARY KEY,
    task_id VARCHAR(36) NOT NULL,
    depends_on_task_id VARCHAR(36) NOT NULL,
    dependency_type VARCHAR(50) DEFAULT 'FINISH_TO_START',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_task_dependency (task_id, depends_on_task_id),
    INDEX idx_task_deps_task (task_id),
    INDEX idx_task_deps_depends_on (depends_on_task_id)
);

-- ============================================
-- DEADLINES
-- ============================================
CREATE TABLE IF NOT EXISTS deadlines (
    id VARCHAR(36) PRIMARY KEY,
    deadline_number VARCHAR(50) NOT NULL,
    case_id VARCHAR(36),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    deadline_type VARCHAR(50) NOT NULL,
    due_date DATE NOT NULL,
    due_time TIME,
    reminder_days JSON,
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'PENDING',
    assigned_to VARCHAR(36),
    assigned_by VARCHAR(36),
    linked_task_id VARCHAR(36),
    linked_document_id VARCHAR(36),
    escalation_level INT DEFAULT 0,
    last_reminder_sent TIMESTAMP,
    reminders_sent JSON,
    completed_at TIMESTAMP,
    completed_by VARCHAR(36),
    completion_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (linked_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (linked_document_id) REFERENCES case_documents(id) ON DELETE SET NULL,
    UNIQUE KEY uk_deadlines_number (deadline_number),
    INDEX idx_deadlines_case (case_id),
    INDEX idx_deadlines_due_date (due_date),
    INDEX idx_deadlines_status (status),
    INDEX idx_deadlines_assigned (assigned_to)
);

-- ============================================
-- CALENDAR EVENTS
-- ============================================
CREATE TABLE IF NOT EXISTS calendar_events (
    id VARCHAR(36) PRIMARY KEY,
    event_number VARCHAR(50) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    all_day BOOLEAN DEFAULT FALSE,
    location VARCHAR(500),
    is_virtual BOOLEAN DEFAULT FALSE,
    virtual_meeting_url VARCHAR(500),
    attendees JSON,
    reminders JSON,
    visibility VARCHAR(50) DEFAULT 'PRIVATE',
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    recurrence_rule VARCHAR(500),
    linked_entity_type VARCHAR(50),
    linked_entity_id VARCHAR(36),
    color VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    UNIQUE KEY uk_calendar_events_number (event_number),
    INDEX idx_calendar_events_user (user_id),
    INDEX idx_calendar_events_case (case_id),
    INDEX idx_calendar_events_date (start_date, end_date)
);

-- ============================================
-- CASE NOTES
-- ============================================
CREATE TABLE IF NOT EXISTS case_notes (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    note_type VARCHAR(50) NOT NULL,
    title VARCHAR(500),
    content TEXT NOT NULL,
    is_confidential BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_shared BOOLEAN DEFAULT FALSE,
    shared_with JSON,
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_case_notes_case (case_id),
    INDEX idx_case_notes_type (note_type)
);

-- ============================================
-- CASE ACTIVITIES
-- ============================================
CREATE TABLE IF NOT EXISTS case_activities (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    activity_title VARCHAR(500) NOT NULL,
    activity_description TEXT,
    entity_type VARCHAR(50),
    entity_id VARCHAR(36),
    performed_by VARCHAR(36),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_case_activities_case (case_id),
    INDEX idx_case_activities_date (performed_at)
);