package com.kinorify.collection.service;

import com.kinorify.collection.dto.response.MergeRequestResponseDTO;

import java.util.List;
import java.util.UUID;

public interface MergeRequestService {

    /**
     * Creates a merge request in PENDING state.
     *
     * The requestor must be an active owner of the source
     * collection. The initial relationship proposal is gathered
     * and stored with the request.
     *
     * No target-owner decisions are created at this point.
     */
    MergeRequestResponseDTO createMergeRequest(
            UUID sourceCollectionId,
            UUID targetCollectionId,
            UUID requestedByProfileId
    );

    MergeRequestResponseDTO getMergeRequestById(
            UUID mergeRequestId
    );

    List<MergeRequestResponseDTO>
    getMergeRequestsByRequestedByProfileId(
            UUID profileId
    );

    List<MergeRequestResponseDTO>
    getPendingMergeRequestsByCollectionId(
            UUID collectionId
    );

    List<MergeRequestResponseDTO>
    getMergeRequestsByStatus(
            String status
    );

    List<MergeRequestResponseDTO>
    getMergeRequestsByTargetCollectionId(
            UUID collectionId
    );

    List<MergeRequestResponseDTO>
    getMergeRequestsBySourceCollectionId(
            UUID collectionId
    );

    boolean existsPendingMergeRequestByCollectionId(
            UUID collectionId
    );

    /**
     * Sends a finalized merge request to every active owner
     * of the target collection.
     *
     * This creates one PENDING decision for every target owner.
     */
    void sendRequestToTargetOwners(
            UUID mergeRequestId
    );

    /**
     * Atomically commits the first owner decision.
     *
     * The merge request row is locked before checking its status.
     * The winning decision is committed, all other pending
     * decisions are cancelled, and the merge request status is
     * updated.
     *
     * If APPROVED, a new collection is created from the final
     * proposal.
     */
    void commitDecision(
            UUID mergeRequestId,
            UUID decisionId,
            UUID profileId,
            String status
    );

    /**
     * Cancels all remaining PENDING decisions for a merge request,
     * excluding the winning decision.
     *
     * The merge request must already have a non-PENDING status.
     */
    void killOtherPendingForThatMR(
            UUID mergeRequestId,
            UUID winningDecisionId
    );

/**
 * Creates the new collection from the finalized merge proposal.
 *
 * The requestor-provided collection name and thumbnail are used
 * for the new collection. The finalized proposed relationships
 * are persisted as the new collection's OWNER, CAN_ADD, and
 * VIEW_ONLY relationships.
 *
 * This is only called after the merge request has been APPROVED.
 */
    UUID createNewMergedCollection(UUID mergeRequestId);
}
