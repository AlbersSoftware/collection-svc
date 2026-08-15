--liquibase formatted sql

--changeset calbers:006

CREATE TABLE collection.collection_lineage
(
    collection_lineage_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    collection_id UUID NOT NULL,

    ancestor_collection_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_collection_lineage_collection_ancestor
        UNIQUE (collection_id, ancestor_collection_id),

    CONSTRAINT chk_collection_lineage_not_self
        CHECK (
            collection_id <> ancestor_collection_id
        )
);
