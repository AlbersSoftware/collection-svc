package com.kinorify.collection.service;

import com.kinorify.collection.dto.FinalProposalDTO;
import com.kinorify.collection.dto.ProposedRelationshipDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.dto.response.MergeRequestDecisionResponseDTO;
import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;

import java.util.List;
import java.util.UUID;

public interface MergeRequestDecisionService {

    /*
     * Proposal workflow
     */

    /**
     * Gets the current proposal associated with a merge request.
     */
    List<ProposedRelationshipDTO> getProposal(UUID mergeRequestId);

    /**
     * Updates the proposal before it has been finalized.
     *
     * Only the merge requestor may modify the proposal.
     */
    List<ProposedRelationshipDTO> updateProposal(
            UUID mergeRequestId,
            UUID profileId,
            List<ProposedRelationshipDTO> proposedRelationships
    );

    /**
     * Finalizes the proposal and sends the finalized proposal to
     * every active owner of the target collection.
     *
 

    /*
     * Decision creation
     */

    /**
     * Creates one PENDING decision for every active target owner.
     *
     * These decisions are independent decision opportunities,
     * not votes. The first owner to commit wins.
     */
    void createPendingDecisions(
            UUID mergeRequestId,
            List<CollectionRelationshipResponseDTO> targetOwners
    );

    /*
     * Decision reads
     */

    MergeRequestDecisionResponseDTO getDecisionById(UUID decisionId);

    List<MergeRequestDecisionResponseDTO> getDecisionsByMergeRequestId(
            UUID mergeRequestId
    );

    List<MergeRequestDecisionResponseDTO> getDecisionsByDeciderProfileId(
            UUID profileId
    );

    String getDecisionStatusById(UUID decisionId);

    /*
     * Decision actions
     */

    /**
     * Attempts to approve a merge request.
     *
     * The first owner to successfully commit a decision wins.
     * All other pending decisions are subsequently cancelled.
     */
    MergeRequestDecisionResponseDTO approve(
            UUID decisionId,
            UUID profileId
    );

    /**
     * Attempts to decline a merge request.
     *
     * The first owner to successfully commit a decision wins.
     * All other pending decisions are subsequently cancelled.
     */
    MergeRequestDecisionResponseDTO decline(
            UUID decisionId,
            UUID profileId
    );

    /**
     * Marks the specified decision as the winning decision.
     *
     * MergeRequestService is responsible for locking the
     * MergeRequest and determining whether this decision is
     * actually allowed to win.
     */
    void winningDecision(
            UUID decisionId,
            UUID profileId,
            MergeRequestDecisionStatus status
    );

    /**
     * Cancels every remaining PENDING decision for the merge
     * request except the winning decision.
     */
    void cancelPendingDecisions(
            UUID mergeRequestId,
            UUID winningDecisionId
    );

    void supplyNameAndThumbnail(
        UUID mergeRequestId,
        UUID profileId,
        String proposedName,
        UUID proposedThumbnailMediaId
);

void sendFinalProposal(
        UUID mergeRequestId,
        UUID profileId,
        FinalProposalDTO finalProposal
);
}
