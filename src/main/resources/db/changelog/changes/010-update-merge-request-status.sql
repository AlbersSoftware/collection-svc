--liquibase formatted sql

--changeset calbers:010

ALTER TABLE collection.merge_request
    DROP CONSTRAINT chk_merge_request_status;

ALTER TABLE collection.merge_request
    ADD CONSTRAINT chk_merge_request_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'DECLINED',
                'CANCELLED'
            )
        );
