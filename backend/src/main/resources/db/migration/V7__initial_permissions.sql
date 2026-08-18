-- ============================================
-- LPMS Initial Permissions and Role Setup
-- Version: 7.0.0
-- ============================================

-- ============================================
-- PERMISSIONS
-- ============================================
INSERT INTO permissions (id, name, code, description, category) VALUES
-- User Management
(UUID(), 'View Users', 'USER_VIEW', 'View user accounts', 'USER'),
(UUID(), 'Create Users', 'USER_CREATE', 'Create new user accounts', 'USER'),
(UUID(), 'Update Users', 'USER_UPDATE', 'Update user accounts', 'USER'),
(UUID(), 'Delete Users', 'USER_DELETE', 'Delete user accounts', 'USER'),
(UUID(), 'Manage User Roles', 'USER_MANAGE_ROLES', 'Assign and manage user roles', 'USER'),

-- Law Firm Management
(UUID(), 'View Law Firms', 'LAW_FIRM_VIEW', 'View law firm information', 'LAW_FIRM'),
(UUID(), 'Create Law Firms', 'LAW_FIRM_CREATE', 'Create new law firms', 'LAW_FIRM'),
(UUID(), 'Update Law Firms', 'LAW_FIRM_UPDATE', 'Update law firm settings', 'LAW_FIRM'),
(UUID(), 'Delete Law Firms', 'LAW_FIRM_DELETE', 'Delete law firms', 'LAW_FIRM'),
(UUID(), 'Manage Law Firm Settings', 'LAW_FIRM_MANAGE_SETTINGS', 'Manage firm-wide settings', 'LAW_FIRM'),

-- Branch Management
(UUID(), 'View Branches', 'BRANCH_VIEW', 'View branch information', 'BRANCH'),
(UUID(), 'Create Branches', 'BRANCH_CREATE', 'Create new branches', 'BRANCH'),
(UUID(), 'Update Branches', 'BRANCH_UPDATE', 'Update branches', 'BRANCH'),
(UUID(), 'Delete Branches', 'BRANCH_DELETE', 'Delete branches', 'BRANCH'),

-- Lawyer Management
(UUID(), 'View Lawyers', 'LAWYER_VIEW', 'View lawyer profiles', 'LAWYER'),
(UUID(), 'Create Lawyers', 'LAWYER_CREATE', 'Create lawyer profiles', 'LAWYER'),
(UUID(), 'Update Lawyers', 'LAWYER_UPDATE', 'Update lawyer profiles', 'LAWYER'),
(UUID(), 'Delete Lawyers', 'LAWYER_DELETE', 'Delete lawyer profiles', 'LAWYER'),
(UUID(), 'Assign Cases to Lawyers', 'LAWYER_ASSIGN_CASES', 'Assign cases to lawyers', 'LAWYER'),

-- Client Management
(UUID(), 'View Clients', 'CLIENT_VIEW', 'View client information', 'CLIENT'),
(UUID(), 'Create Clients', 'CLIENT_CREATE', 'Create new clients', 'CLIENT'),
(UUID(), 'Update Clients', 'CLIENT_UPDATE', 'Update client information', 'CLIENT'),
(UUID(), 'Delete Clients', 'CLIENT_DELETE', 'Delete clients', 'CLIENT'),
(UUID(), 'Run Conflict Checks', 'CLIENT_CONFLICT_CHECK', 'Run conflict of interest checks', 'CLIENT'),

-- Case Management
(UUID(), 'View Cases', 'CASE_VIEW', 'View case information', 'CASE'),
(UUID(), 'Create Cases', 'CASE_CREATE', 'Create new cases', 'CASE'),
(UUID(), 'Update Cases', 'CASE_UPDATE', 'Update case information', 'CASE'),
(UUID(), 'Delete Cases', 'CASE_DELETE', 'Delete cases', 'CASE'),
(UUID(), 'Close Cases', 'CASE_CLOSE', 'Close cases', 'CASE'),
(UUID(), 'Archive Cases', 'CASE_ARCHIVE', 'Archive cases', 'CASE'),
(UUID(), 'Manage Case Parties', 'CASE_MANAGE_PARTIES', 'Manage case parties', 'CASE'),
(UUID(), 'View Case Timeline', 'CASE_VIEW_TIMELINE', 'View case timeline', 'CASE'),

-- Court Management
(UUID(), 'View Courts', 'COURT_VIEW', 'View court information', 'COURT'),
(UUID(), 'Create Courts', 'COURT_CREATE', 'Create new courts', 'COURT'),
(UUID(), 'Update Courts', 'COURT_UPDATE', 'Update court information', 'COURT'),
(UUID(), 'Delete Courts', 'COURT_DELETE', 'Delete courts', 'COURT'),
(UUID(), 'Manage Judges', 'COURT_MANAGE_JUDGES', 'Manage judges', 'COURT'),

-- Hearing Management
(UUID(), 'View Hearings', 'HEARING_VIEW', 'View hearing schedules', 'HEARING'),
(UUID(), 'Create Hearings', 'HEARING_CREATE', 'Schedule new hearings', 'HEARING'),
(UUID(), 'Update Hearings', 'HEARING_UPDATE', 'Update hearing details', 'HEARING'),
(UUID(), 'Delete Hearings', 'HEARING_DELETE', 'Cancel hearings', 'HEARING'),

-- Document Management
(UUID(), 'View Documents', 'DOCUMENT_VIEW', 'View documents', 'DOCUMENT'),
(UUID(), 'Upload Documents', 'DOCUMENT_UPLOAD', 'Upload new documents', 'DOCUMENT'),
(UUID(), 'Download Documents', 'DOCUMENT_DOWNLOAD', 'Download documents', 'DOCUMENT'),
(UUID(), 'Delete Documents', 'DOCUMENT_DELETE', 'Delete documents', 'DOCUMENT'),
(UUID(), 'Share Documents', 'DOCUMENT_SHARE', 'Share documents', 'DOCUMENT'),
(UUID(), 'View Document Versions', 'DOCUMENT_VIEW_VERSIONS', 'View document versions', 'DOCUMENT'),

-- Evidence Management
(UUID(), 'View Evidence', 'EVIDENCE_VIEW', 'View evidence', 'EVIDENCE'),
(UUID(), 'Add Evidence', 'EVIDENCE_ADD', 'Add new evidence', 'EVIDENCE'),
(UUID(), 'Update Evidence', 'EVIDENCE_UPDATE', 'Update evidence', 'EVIDENCE'),
(UUID(), 'Delete Evidence', 'EVIDENCE_DELETE', 'Delete evidence', 'EVIDENCE'),
(UUID(), 'Manage Chain of Custody', 'EVIDENCE_CHAIN_OF_CUSTODY', 'Manage evidence chain of custody', 'EVIDENCE'),

-- Task Management
(UUID(), 'View Tasks', 'TASK_VIEW', 'View tasks', 'TASK'),
(UUID(), 'Create Tasks', 'TASK_CREATE', 'Create new tasks', 'TASK'),
(UUID(), 'Update Tasks', 'TASK_UPDATE', 'Update tasks', 'TASK'),
(UUID(), 'Delete Tasks', 'TASK_DELETE', 'Delete tasks', 'TASK'),
(UUID(), 'Complete Tasks', 'TASK_COMPLETE', 'Mark tasks as complete', 'TASK'),

-- Deadline Management
(UUID(), 'View Deadlines', 'DEADLINE_VIEW', 'View deadlines', 'DEADLINE'),
(UUID(), 'Create Deadlines', 'DEADLINE_CREATE', 'Create new deadlines', 'DEADLINE'),
(UUID(), 'Update Deadlines', 'DEADLINE_UPDATE', 'Update deadlines', 'DEADLINE'),
(UUID(), 'Delete Deadlines', 'DEADLINE_DELETE', 'Delete deadlines', 'DEADLINE'),

-- Billing
(UUID(), 'View Billing', 'BILLING_VIEW', 'View billing information', 'BILLING'),
(UUID(), 'Create Time Entries', 'BILLING_CREATE_TIME', 'Create time entries', 'BILLING'),
(UUID(), 'Generate Invoices', 'BILLING_CREATE_INVOICE', 'Generate invoices', 'BILLING'),
(UUID(), 'Manage Retainers', 'BILLING_MANAGE_RETAINERS', 'Manage retainer accounts', 'BILLING'),
(UUID(), 'Process Payments', 'PAYMENT_PROCESS', 'Process payments', 'BILLING'),
(UUID(), 'View Reports', 'BILLING_VIEW_REPORTS', 'View billing reports', 'BILLING'),

-- Expense Management
(UUID(), 'View Expenses', 'EXPENSE_VIEW', 'View expenses', 'EXPENSE'),
(UUID(), 'Create Expenses', 'EXPENSE_CREATE', 'Submit expenses', 'EXPENSE'),
(UUID(), 'Approve Expenses', 'EXPENSE_APPROVE', 'Approve expense claims', 'EXPENSE'),
(UUID(), 'Reject Expenses', 'EXPENSE_REJECT', 'Reject expense claims', 'EXPENSE'),

-- Contract Management
(UUID(), 'View Contracts', 'CONTRACT_VIEW', 'View contracts', 'CONTRACT'),
(UUID(), 'Create Contracts', 'CONTRACT_CREATE', 'Create contracts', 'CONTRACT'),
(UUID(), 'Update Contracts', 'CONTRACT_UPDATE', 'Update contracts', 'CONTRACT'),
(UUID(), 'Delete Contracts', 'CONTRACT_DELETE', 'Delete contracts', 'CONTRACT'),
(UUID(), 'Sign Contracts', 'CONTRACT_SIGN', 'Sign contracts', 'CONTRACT'),

-- Communication
(UUID(), 'View Messages', 'MESSAGE_VIEW', 'View messages', 'COMMUNICATION'),
(UUID(), 'Send Messages', 'MESSAGE_SEND', 'Send messages', 'COMMUNICATION'),
(UUID(), 'Manage Conversations', 'MESSAGE_MANAGE', 'Manage conversations', 'COMMUNICATION'),

-- Notifications
(UUID(), 'View Notifications', 'NOTIFICATION_VIEW', 'View notifications', 'NOTIFICATION'),
(UUID(), 'Manage Notifications', 'NOTIFICATION_MANAGE', 'Manage notification settings', 'NOTIFICATION'),

-- AI Features
(UUID(), 'Use AI Assistant', 'AI_USE', 'Use AI assistant', 'AI'),
(UUID(), 'Perform AI Research', 'AI_RESEARCH', 'Perform legal research with AI', 'AI'),
(UUID(), 'AI Document Analysis', 'AI_DOCUMENT_ANALYSIS', 'Analyze documents with AI', 'AI'),
(UUID(), 'View AI Usage Reports', 'AI_VIEW_REPORTS', 'View AI usage reports', 'AI'),

-- Audit
(UUID(), 'View Audit Logs', 'AUDIT_VIEW', 'View audit logs', 'AUDIT'),
(UUID(), 'Export Audit Logs', 'AUDIT_EXPORT', 'Export audit logs', 'AUDIT'),

-- Reports
(UUID(), 'View Reports', 'REPORT_VIEW', 'View system reports', 'REPORT'),
(UUID(), 'Export Reports', 'REPORT_EXPORT', 'Export reports', 'REPORT'),
(UUID(), 'Create Custom Reports', 'REPORT_CREATE', 'Create custom reports', 'REPORT'),

-- System
(UUID(), 'System Configuration', 'SYSTEM_CONFIG', 'Configure system settings', 'SYSTEM'),
(UUID(), 'Manage Integrations', 'SYSTEM_INTEGRATIONS', 'Manage system integrations', 'SYSTEM'),
(UUID(), 'View System Health', 'SYSTEM_HEALTH', 'View system health', 'SYSTEM');

-- ============================================
-- ROLES WITH PERMISSIONS
-- ============================================

-- SUPER_ADMIN Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-SUPER-ADMIN', 'Super Administrator', 'Full system access with all permissions', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-SUPER-ADMIN', id FROM permissions;

-- FIRM_ADMIN Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-FIRM-ADMIN', 'Firm Administrator', 'Full access within the law firm', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-FIRM-ADMIN', id FROM permissions
WHERE code LIKE 'USER_%'
   OR code LIKE 'LAW_FIRM_UPDATE'
   OR code LIKE 'LAW_FIRM_MANAGE%'
   OR code LIKE 'BRANCH_%'
   OR code LIKE 'LAWYER_%'
   OR code LIKE 'CLIENT_%'
   OR code LIKE 'CASE_%'
   OR code LIKE 'COURT_%'
   OR code LIKE 'HEARING_%'
   OR code LIKE 'DOCUMENT_%'
   OR code LIKE 'EVIDENCE_%'
   OR code LIKE 'TASK_%'
   OR code LIKE 'DEADLINE_%'
   OR code LIKE 'BILLING_%'
   OR code LIKE 'PAYMENT_%'
   OR code LIKE 'EXPENSE_%'
   OR code LIKE 'CONTRACT_%'
   OR code LIKE 'MESSAGE_%'
   OR code LIKE 'NOTIFICATION_%'
   OR code LIKE 'REPORT_%'
   OR code LIKE 'AUDIT_VIEW';

-- MANAGING_PARTNER Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-MANAGING-PARTNER', 'Managing Partner', 'Senior partner with management duties', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-MANAGING-PARTNER', id FROM permissions
WHERE code LIKE 'CASE_%'
   OR code LIKE 'CLIENT_%'
   OR code LIKE 'LAWYER_%'
   OR code LIKE 'COURT_%'
   OR code LIKE 'HEARING_%'
   OR code LIKE 'DOCUMENT_%'
   OR code LIKE 'EVIDENCE_%'
   OR code LIKE 'TASK_%'
   OR code LIKE 'DEADLINE_%'
   OR code LIKE 'BILLING_VIEW'
   OR code LIKE 'PAYMENT_PROCESS'
   OR code LIKE 'CONTRACT_%'
   OR code LIKE 'MESSAGE_%'
   OR code LIKE 'NOTIFICATION_%'
   OR code LIKE 'REPORT_VIEW'
   OR code LIKE 'AUDIT_VIEW'
   OR code LIKE 'AI_%';

-- SENIOR_LAWYER Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-SENIOR-LAWYER', 'Senior Lawyer', 'Experienced lawyer with supervisory duties', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-SENIOR-LAWYER', id FROM permissions
WHERE code IN (
    'CASE_VIEW', 'CASE_CREATE', 'CASE_UPDATE', 'CASE_MANAGE_PARTIES', 'CASE_VIEW_TIMELINE',
    'CLIENT_VIEW', 'CLIENT_CREATE', 'CLIENT_UPDATE',
    'COURT_VIEW', 'HEARING_VIEW', 'HEARING_CREATE', 'HEARING_UPDATE',
    'DOCUMENT_VIEW', 'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD', 'DOCUMENT_SHARE',
    'EVIDENCE_VIEW', 'EVIDENCE_ADD', 'EVIDENCE_UPDATE',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_UPDATE', 'TASK_COMPLETE',
    'DEADLINE_VIEW', 'DEADLINE_CREATE', 'DEADLINE_UPDATE',
    'BILLING_VIEW', 'BILLING_CREATE_TIME', 'BILLING_CREATE_INVOICE',
    'PAYMENT_PROCESS',
    'CONTRACT_VIEW', 'CONTRACT_CREATE', 'CONTRACT_UPDATE', 'CONTRACT_SIGN',
    'MESSAGE_VIEW', 'MESSAGE_SEND',
    'NOTIFICATION_VIEW',
    'AI_USE', 'AI_RESEARCH', 'AI_DOCUMENT_ANALYSIS',
    'REPORT_VIEW'
);

-- LAWYER Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-LAWYER', 'Lawyer', 'Standard lawyer with case responsibilities', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-LAWYER', id FROM permissions
WHERE code IN (
    'CASE_VIEW', 'CASE_UPDATE', 'CASE_MANAGE_PARTIES', 'CASE_VIEW_TIMELINE',
    'CLIENT_VIEW', 'CLIENT_CREATE', 'CLIENT_UPDATE',
    'COURT_VIEW', 'HEARING_VIEW', 'HEARING_CREATE',
    'DOCUMENT_VIEW', 'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD',
    'EVIDENCE_VIEW', 'EVIDENCE_ADD',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_UPDATE', 'TASK_COMPLETE',
    'DEADLINE_VIEW', 'DEADLINE_CREATE',
    'BILLING_VIEW', 'BILLING_CREATE_TIME',
    'CONTRACT_VIEW', 'CONTRACT_SIGN',
    'MESSAGE_VIEW', 'MESSAGE_SEND',
    'NOTIFICATION_VIEW',
    'AI_USE', 'AI_RESEARCH', 'AI_DOCUMENT_ANALYSIS'
);

-- PARALEGAL Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-PARALEGAL', 'Paralegal', 'Support staff for legal work', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-PARALEGAL', id FROM permissions
WHERE code IN (
    'CASE_VIEW', 'CASE_VIEW_TIMELINE',
    'CLIENT_VIEW',
    'COURT_VIEW', 'HEARING_VIEW',
    'DOCUMENT_VIEW', 'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD',
    'EVIDENCE_VIEW', 'EVIDENCE_ADD',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_UPDATE', 'TASK_COMPLETE',
    'DEADLINE_VIEW',
    'BILLING_VIEW', 'BILLING_CREATE_TIME',
    'CONTRACT_VIEW',
    'MESSAGE_VIEW', 'MESSAGE_SEND',
    'NOTIFICATION_VIEW'
);

-- LEGAL_ASSISTANT Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-LEGAL-ASSISTANT', 'Legal Assistant', 'Administrative support for legal work', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-LEGAL-ASSISTANT', id FROM permissions
WHERE code IN (
    'CASE_VIEW',
    'CLIENT_VIEW',
    'COURT_VIEW', 'HEARING_VIEW',
    'DOCUMENT_VIEW', 'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_UPDATE',
    'DEADLINE_VIEW',
    'CONTRACT_VIEW',
    'MESSAGE_VIEW', 'MESSAGE_SEND',
    'NOTIFICATION_VIEW'
);

-- ACCOUNTANT Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-ACCOUNTANT', 'Accountant', 'Financial management and billing', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-ACCOUNTANT', id FROM permissions
WHERE code IN (
    'CLIENT_VIEW',
    'CASE_VIEW',
    'BILLING_VIEW', 'BILLING_CREATE_TIME', 'BILLING_CREATE_INVOICE', 'BILLING_MANAGE_RETAINERS', 'BILLING_VIEW_REPORTS',
    'PAYMENT_PROCESS',
    'EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_APPROVE', 'EXPENSE_REJECT',
    'RETAINERS_VIEW',
    'REPORT_VIEW', 'REPORT_EXPORT'
);

-- RECEPTIONIST Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-RECEPTIONIST', 'Receptionist', 'Front desk and client intake', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-RECEPTIONIST', id FROM permissions
WHERE code IN (
    'CLIENT_VIEW', 'CLIENT_CREATE',
    'COURT_VIEW', 'HEARING_VIEW',
    'DOCUMENT_VIEW',
    'MESSAGE_VIEW', 'MESSAGE_SEND',
    'NOTIFICATION_VIEW'
);

-- CLIENT Role
INSERT INTO roles (id, name, description, is_system_role) VALUES
('ROLE-CLIENT', 'Client', 'External client access', TRUE);

INSERT INTO role_permissions (id, role_id, permission_id)
SELECT UUID(), 'ROLE-CLIENT', id FROM permissions
WHERE code IN (
    'CLIENT_VIEW_OWN',
    'CASE_VIEW_OWN',
    'DOCUMENT_VIEW_OWN', 'DOCUMENT_DOWNLOAD_OWN',
    'HEARING_VIEW_OWN',
    'INVOICE_VIEW_OWN',
    'PAYMENT_CREATE_OWN',
    'MESSAGE_VIEW_OWN', 'MESSAGE_SEND_OWN',
    'NOTIFICATION_VIEW_OWN'
);