--liquibase formatted sql

--changeset calbers:008

/*
 * A merge request has one decision opportunity per target owner.
 *
 * The previous constraint allowed only one decision for the
 * entire merge request, which is incompatible with the
 * first-come-first-served owner decision model.
 */
ALTER TABLE collection.merge_request_decision
    DROP CONSTRAINT IF EXISTS uk_merge_request_decision_request;

/*
 * Convert the old ACCEPTED status to APPROVED.
 */
UPDATE collection.merge_request_decision
SET status = 'APPROVED'
WHERE status = 'ACCEPTED';

/*
 * Replace the old decision status constraint.
 */
ALTER TABLE collection.merge_request_decision
    DROP CONSTRAINT IF EXISTS chk_merge_request_decision_status;

ALTER TABLE collection.merge_request_decision
    ADD CONSTRAINT chk_merge_request_decision_status
    CHECK (
        status IN (
            'PENDING',
            'APPROVED',
            'DECLINED',
            'CANCELLED'
        )
    );

/*
 * An owner may have only one decision record for a particular
 * merge request.
 */
ALTER TABLE collection.merge_request_decision
    ADD CONSTRAINT uk_merge_request_decision_owner
    UNIQUE (
        merge_request_id,
        decider_profile_id
    );
