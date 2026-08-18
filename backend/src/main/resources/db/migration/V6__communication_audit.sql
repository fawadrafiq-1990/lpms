-- ============================================
-- LPMS Communication, Notifications, AI, and Audit
-- Version: 6.0.0
-- ============================================

-- ============================================
-- NOTIFICATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(50) DEFAULT 'NORMAL',
    channel VARCHAR(50) DEFAULT 'IN_APP',
    status VARCHAR(50) DEFAULT 'UNREAD',
    read_at TIMESTAMP,
    read_by VARCHAR(36),
    action_url VARCHAR(500),
    action_type VARCHAR(50),
    action_id VARCHAR(36),
    entity_type VARCHAR(50),
    entity_id VARCHAR(36),
    case_id VARCHAR(36),
    metadata JSON,
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (read_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_type (notification_type),
    INDEX idx_notifications_created (created_at)
);

-- ============================================
-- NOTIFICATION PREFERENCES
-- ============================================
CREATE TABLE IF NOT EXISTS notification_preferences (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_notification_prefs_user_type (user_id, notification_type)
);

-- ============================================
-- CONVERSATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(36) PRIMARY KEY,
    conversation_type VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    is_group BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    last_message_at TIMESTAMP,
    last_message_preview VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version INT DEFAULT 0,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_conversations_case (case_id),
    INDEX idx_conversations_client (client_id),
    INDEX idx_conversations_type (conversation_type)
);

-- ============================================
-- CONVERSATION PARTICIPANTS
-- ============================================
CREATE TABLE IF NOT EXISTS conversation_participants (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    participant_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(36),
    client_id VARCHAR(36),
    role VARCHAR(50) DEFAULT 'MEMBER',
    is_typing BOOLEAN DEFAULT FALSE,
    last_read_at TIMESTAMP,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    UNIQUE KEY uk_conv_participant (conversation_id, user_id, client_id),
    INDEX idx_conv_participants_conv (conversation_id)
);

-- ============================================
-- MESSAGES
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender_type VARCHAR(50) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    content TEXT NOT NULL,
    content_html TEXT,
    attachments JSON,
    metadata JSON,
    is_system_message BOOLEAN DEFAULT FALSE,
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_starred BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    edited_at TIMESTAMP,
    reply_to_id VARCHAR(36),
    case_id VARCHAR(36),
    read_by JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (reply_to_id) REFERENCES messages(id) ON DELETE SET NULL,
    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_created (created_at)
);

-- ============================================
-- AI CONVERSATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS ai_conversations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    conversation_type VARCHAR(50) DEFAULT 'GENERAL',
    title VARCHAR(500),
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    context JSON,
    is_archived BOOLEAN DEFAULT FALSE,
    last_message_at TIMESTAMP,
    message_count INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    estimated_cost DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    INDEX idx_ai_conversations_user (user_id),
    INDEX idx_ai_conversations_case (case_id)
);

-- ============================================
-- AI MESSAGES
-- ============================================
CREATE TABLE IF NOT EXISTS ai_messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    content_type VARCHAR(50) DEFAULT 'TEXT',
    model VARCHAR(100),
    tokens_used INT,
    input_tokens INT,
    output_tokens INT,
    latency_ms INT,
    sources JSON,
    agent_chain JSON,
    tool_calls JSON,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    INDEX idx_ai_messages_conv (conversation_id)
);

-- ============================================
-- AI AGENT RUNS
-- ============================================
CREATE TABLE IF NOT EXISTS ai_agent_runs (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36),
    message_id VARCHAR(36),
    agent_type VARCHAR(100) NOT NULL,
    agent_name VARCHAR(255),
    input JSON NOT NULL,
    output JSON,
    tools_used JSON,
    sources JSON,
    tokens_used INT DEFAULT 0,
    latency_ms INT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE SET NULL,
    FOREIGN KEY (message_id) REFERENCES ai_messages(id) ON DELETE SET NULL,
    INDEX idx_ai_agent_runs_conv (conversation_id),
    INDEX idx_ai_agent_runs_agent (agent_type)
);

-- ============================================
-- AI USAGE
-- ============================================
CREATE TABLE IF NOT EXISTS ai_usage (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    conversation_id VARCHAR(36),
    agent_type VARCHAR(100),
    model VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    request_type VARCHAR(100),
    input_tokens INT DEFAULT 0,
    output_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    latency_ms INT,
    estimated_cost DECIMAL(10,4) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    INDEX idx_ai_usage_user (user_id),
    INDEX idx_ai_usage_firm (law_firm_id),
    INDEX idx_ai_usage_created (created_at),
    INDEX idx_ai_usage_model (model)
);

-- ============================================
-- AI TOOL CALLS
-- ============================================
CREATE TABLE IF NOT EXISTS ai_tool_calls (
    id VARCHAR(36) PRIMARY KEY,
    agent_run_id VARCHAR(36) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    tool_arguments JSON,
    tool_result JSON,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    error_message TEXT,
    execution_time_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_run_id) REFERENCES ai_agent_runs(id) ON DELETE CASCADE,
    INDEX idx_ai_tool_calls_run (agent_run_id)
);

-- ============================================
-- LEGAL RESEARCH
-- ============================================
CREATE TABLE IF NOT EXISTS legal_research (
    id VARCHAR(36) PRIMARY KEY,
    research_number VARCHAR(50) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    title VARCHAR(500) NOT NULL,
    query TEXT NOT NULL,
    query_embedding JSON,
    results JSON,
    summary TEXT,
    sources JSON,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    total_sources INT DEFAULT 0,
    relevant_sources INT DEFAULT 0,
    tokens_used INT,
    execution_time_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    UNIQUE KEY uk_legal_research_number (research_number),
    INDEX idx_legal_research_user (user_id),
    INDEX idx_legal_research_case (case_id)
);

-- ============================================
-- LEGAL CITATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS legal_citations (
    id VARCHAR(36) PRIMARY KEY,
    research_id VARCHAR(36) NOT NULL,
    citation_type VARCHAR(50) NOT NULL,
    citation_text TEXT NOT NULL,
    source_title VARCHAR(500),
    source_url VARCHAR(500),
    source_type VARCHAR(50),
    relevance_score DECIMAL(5,4),
    page_number VARCHAR(50),
    paragraph VARCHAR(50),
    quoted_text TEXT,
    context TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (research_id) REFERENCES legal_research(id) ON DELETE CASCADE,
    INDEX idx_legal_citations_research (research_id)
);

-- ============================================
-- LEGAL PRECEDENTS
-- ============================================
CREATE TABLE IF NOT EXISTS legal_precedents (
    id VARCHAR(36) PRIMARY KEY,
    case_title VARCHAR(500) NOT NULL,
    case_number VARCHAR(100),
    court VARCHAR(255),
    judge VARCHAR(255),
    decision_date DATE,
    citation VARCHAR(255),
    citation_url VARCHAR(500),
    keywords JSON,
    practice_area VARCHAR(100),
    legal_principles TEXT,
    summary TEXT,
    relevance_tags JSON,
    case_summary_embedding JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    INDEX idx_legal_precedents_court (court),
    INDEX idx_legal_precedents_practice (practice_area),
    INDEX idx_legal_precedents_date (decision_date)
);

-- ============================================
-- AUDIT LOGS
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    law_firm_id VARCHAR(36),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36),
    entity_name VARCHAR(500),
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    document_id VARCHAR(36),
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    session_id VARCHAR(100),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_logs_user (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_case (case_id),
    INDEX idx_audit_logs_created (created_at)
);

-- ============================================
-- WORKFLOW DEFINITIONS
-- ============================================
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id VARCHAR(36) PRIMARY KEY,
    workflow_type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    steps JSON NOT NULL,
    triggers JSON,
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    version_lock INT DEFAULT 0,
    INDEX idx_workflow_defs_type (workflow_type),
    INDEX idx_workflow_defs_active (is_active)
);

-- ============================================
-- WORKFLOW INSTANCES
-- ============================================
CREATE TABLE IF NOT EXISTS workflow_instances (
    id VARCHAR(36) PRIMARY KEY,
    workflow_definition_id VARCHAR(36) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    current_step INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    context JSON,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    due_date DATE,
    version INT DEFAULT 0,
    FOREIGN KEY (workflow_definition_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    INDEX idx_workflow_instances_def (workflow_definition_id),
    INDEX idx_workflow_instances_entity (entity_type, entity_id),
    INDEX idx_workflow_instances_status (status)
);

-- ============================================
-- WORKFLOW STEPS
-- ============================================
CREATE TABLE IF NOT EXISTS workflow_steps (
    id VARCHAR(36) PRIMARY KEY,
    instance_id VARCHAR(36) NOT NULL,
    step_order INT NOT NULL,
    step_type VARCHAR(100) NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    assignee_id VARCHAR(36),
    status VARCHAR(50) DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    output JSON,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instance_id) REFERENCES workflow_instances(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_workflow_steps_instance (instance_id)
);

-- ============================================
-- WORKFLOW ACTIONS
-- ============================================
CREATE TABLE IF NOT EXISTS workflow_actions (
    id VARCHAR(36) PRIMARY KEY,
    step_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    action_data JSON,
    performed_by VARCHAR(36),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (step_id) REFERENCES workflow_steps(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_workflow_actions_step (step_id)
);

-- ============================================
-- CONFLICT CHECKS
-- ============================================
CREATE TABLE IF NOT EXISTS conflict_checks (
    id VARCHAR(36) PRIMARY KEY,
    check_number VARCHAR(50) NOT NULL,
    law_firm_id VARCHAR(36) NOT NULL,
    case_id VARCHAR(36),
    client_id VARCHAR(36),
    search_query TEXT NOT NULL,
    search_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING',
    result VARCHAR(50) DEFAULT 'PENDING',
    result_details JSON,
    conflicting_entities JSON,
    checked_by VARCHAR(36),
    checked_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (law_firm_id) REFERENCES law_firms(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (checked_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_conflict_checks_number (check_number),
    INDEX idx_conflict_checks_firm (law_firm_id),
    INDEX idx_conflict_checks_case (case_id)
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX idx_cases_created_at ON cases(created_at);
CREATE INDEX idx_clients_created_at ON clients(created_at);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_deadlines_created_at ON deadlines(created_at);
CREATE INDEX idx_invoices_created_at ON invoices(created_at);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_audit_logs_firm_created ON audit_logs(law_firm_id, created_at);