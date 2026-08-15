--liquibase formatted sql

--changeset calbers:004

CREATE TABLE collection.merge_request
(
    merge_request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    source_collection_id UUID NOT NULL,

    target_collection_id UUID NOT NULL,

    requested_by_profile_id UUID NOT NULL,

    proposed_relationships JSONB NOT NULL,

    new_collection_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    responded_at TIMESTAMPTZ,

    CONSTRAINT chk_merge_request_status
        CHECK (
            status IN (
                'PENDING',
                'ACCEPTED',
                'DECLINED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_merge_request_different_collections
        CHECK (
            source_collection_id <> target_collection_id
        )
);
