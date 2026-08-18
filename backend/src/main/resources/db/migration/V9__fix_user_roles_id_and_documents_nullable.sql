-- ============================================
-- V9: Allow metadata-only documents & fix user_roles.id default
-- ============================================

-- The user_roles join table has an `id` PK with NO default. Hibernate's
-- @ManyToMany insert only populates user_id + role_id, so creating a user
-- with a role fails with "Field 'id' doesn't have a default value".
-- MySQL 8 supports DEFAULT (UUID()) so the DB generates the join-row id.
ALTER TABLE user_roles
    MODIFY COLUMN id VARCHAR(36) NOT NULL DEFAULT (UUID());

-- Document columns file_path / file_name / file_size / mime_type are NOT NULL
-- in V4, but metadata-only document records (created before a file is uploaded)
-- have none of these. Make them nullable so a document record can exist without
-- an attached file. The service still supplies defaults, but this removes the
-- hard DB constraint that breaks metadata-only creation.
ALTER TABLE case_documents
    MODIFY COLUMN file_path VARCHAR(512) NULL,
    MODIFY COLUMN file_name VARCHAR(255) NULL,
    MODIFY COLUMN file_size BIGINT NULL,
    MODIFY COLUMN mime_type VARCHAR(255) NULL;
