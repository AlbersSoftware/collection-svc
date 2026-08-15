--liquibase formatted sql

--changeset calbers:002

CREATE TABLE collection.collection_relationships
(
    collection_relationship_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    collection_id UUID NOT NULL,

    profile_id UUID NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'VIEW_ONLY',

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    invited_by_profile_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_collection_relationship_collection_profile
        UNIQUE (collection_id, profile_id),

    CONSTRAINT chk_collection_relationship_role
        CHECK (
            role IN (
                'OWNER',
                'CAN_ADD',
                'VIEW_ONLY'
            )
        ),

    CONSTRAINT chk_collection_relationship_status
        CHECK (
            status IN (
                'PENDING',
                'ACTIVE',
                'DECLINED',
                'REVOKED'
            )
        )
);
