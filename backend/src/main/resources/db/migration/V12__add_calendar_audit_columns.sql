-- ============================================
-- V12: Add missing audit columns to calendar_events
-- ============================================
-- CalendarEvent extends BaseEntity, which maps created_by, updated_by,
-- deleted_at, version. The calendar_events table (V6) has created_at,
-- updated_at, created_by and version but is missing updated_by and
-- deleted_at. Add them so Hibernate can read/write the entity.

ALTER TABLE calendar_events
    ADD COLUMN updated_by VARCHAR(36) NULL AFTER created_by,
    ADD COLUMN deleted_at TIMESTAMP NULL AFTER updated_at;
