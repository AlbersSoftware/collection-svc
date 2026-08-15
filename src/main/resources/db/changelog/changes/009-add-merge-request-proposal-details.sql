--liquibase formatted sql

--changeset calbers:009

ALTER TABLE collection.merge_request
    ADD COLUMN proposed_name VARCHAR(255),
    ADD COLUMN proposed_thumbnail_media_id UUID;
