--liquibase formatted sql

--changeset calbers:007

CREATE INDEX idx_collection_relationships_profile
    ON collection.collection_relationships(profile_id);

CREATE INDEX idx_collection_relationships_collection_status
    ON collection.collection_relationships(collection_id, status);

CREATE INDEX idx_collection_media_collection
    ON collection.collection_media(collection_id);

CREATE INDEX idx_collection_media_media
    ON collection.collection_media(media_id);

CREATE INDEX idx_collection_media_added_by
    ON collection.collection_media(added_by_profile_id);

CREATE INDEX idx_collection_lineage_ancestor
    ON collection.collection_lineage(ancestor_collection_id);
