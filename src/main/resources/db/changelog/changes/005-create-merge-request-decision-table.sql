--liquibase formatted sql

--changeset calbers:005

CREATE TABLE collection.merge_request_decision
(
    merge_request_decision_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    merge_request_id UUID NOT NULL,

    decider_profile_id UUID NOT NULL,

    status VARCHAR(20) NOT NULL,

    note TEXT,

    responded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_merge_request_decision_request
        UNIQUE (merge_request_id),

    CONSTRAINT chk_merge_request_decision_status
        CHECK (
            status IN (
                'ACCEPTED',
                'DECLINED'
            )
        ),

    CONSTRAINT fk_merge_request_decision_request
        FOREIGN KEY (merge_request_id)
        REFERENCES collection.merge_request(merge_request_id)
        ON DELETE CASCADE
);


