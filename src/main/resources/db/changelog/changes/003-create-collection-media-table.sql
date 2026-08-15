--liquibase formatted sql

--changeset calbers:003

CREATE TABLE collection.collection_media
(
    collection_media_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    collection_id UUID NOT NULL,

    media_id UUID NOT NULL,

    added_by_profile_id UUID NOT NULL,

    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    removed_at TIMESTAMPTZ,

    removed_by_profile_id UUID
);
