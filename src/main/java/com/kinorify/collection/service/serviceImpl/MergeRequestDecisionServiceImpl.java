package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.FinalProposalDTO;
import com.kinorify.collection.dto.ProposedRelationshipDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.dto.response.MergeRequestDecisionResponseDTO;
import com.kinorify.collection.entity.MergeRequest;
import com.kinorify.collection.entity.MergeRequestDecision;
import com.kinorify.collection.entity.ProposedRelationship;
import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;
import com.kinorify.collection.entity.enums.MergeRequestStatus;
import com.kinorify.collection.repository.MergeRequestDecisionRepository;
import com.kinorify.collection.repository.MergeRequestRepository;
import com.kinorify.collection.service.CollectionRelationshipService;
import com.kinorify.collection.service.MergeRequestDecisionService;
import com.kinorify.collection.service.MergeRequestService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MergeRequestDecisionServiceImpl
        implements MergeRequestDecisionService {

    private final MergeRequestDecisionRepository decisionRepository;

    private final MergeRequestRepository mergeRequestRepository;

    private final CollectionRelationshipService
            collectionRelationshipService;

    private final MergeRequestService mergeRequestService;

    /*
     * -------------------------------------------------------------------------
     * Proposal workflow
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional(readOnly = true)
    public List<ProposedRelationshipDTO> getProposal(UUID mergeRequestId) {
        MergeRequest mergeRequest =
                getMergeRequestById(mergeRequestId);

        return mergeRequest
                .getProposedRelationships()
                .stream()
                .map(this::mapToProposalDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<ProposedRelationshipDTO> updateProposal(
            UUID mergeRequestId,
            UUID profileId,
            List<ProposedRelationshipDTO> proposedRelationships
    ) {
        MergeRequest mergeRequest =
                getMergeRequestById(mergeRequestId);

        validateRequestor(
                mergeRequest,
                profileId
        );

        ensureProposalEditable(
                mergeRequest
        );

        validateProposal(
                proposedRelationships
        );

        List<ProposedRelationship> relationships =
                proposedRelationships
                        .stream()
                        .map(this::mapToEntity)
                        .toList();

        /*
         * Update only the proposed relationship portion of the
         * draft proposal.
         *
         * Name and thumbnail are managed separately through
         * supplyNameAndThumbnail().
         */
        mergeRequest.setProposedRelationships(
                relationships
        );

        mergeRequestRepository.save(
                mergeRequest
        );

        return proposedRelationships;
    }

    @Override
    @Transactional
    public void supplyNameAndThumbnail(
            UUID mergeRequestId,
            UUID profileId,
            String proposedName,
            UUID proposedThumbnailMediaId
    ) {
        MergeRequest mergeRequest =
                getMergeRequestById(mergeRequestId);

        validateRequestor(
                mergeRequest,
                profileId
        );

        ensureProposalEditable(
                mergeRequest
        );

        if (proposedName == null
                || proposedName.isBlank()) {

            throw new IllegalArgumentException(
                    "Proposed collection name cannot be blank"
            );
        }

        /*
         * The thumbnail is optional.
         */
        mergeRequest.setProposedName(
                proposedName.trim()
        );

        mergeRequest.setProposedThumbnailMediaId(
                proposedThumbnailMediaId
        );

        mergeRequestRepository.save(
                mergeRequest
        );
    }

    @Override
    @Transactional
    public void sendFinalProposal(
            UUID mergeRequestId,
            UUID profileId,
            FinalProposalDTO finalProposal
    ) {
        MergeRequest mergeRequest =
                getMergeRequestById(mergeRequestId);

        validateRequestor(
                mergeRequest,
                profileId
        );

        /*
         * Once target-owner decisions exist, this proposal
         * can no longer be modified or finalized again.
         */
        ensureProposalEditable(
                mergeRequest
        );

        if (finalProposal == null) {
            throw new IllegalArgumentException(
                    "Final proposal cannot be null"
            );
        }

        if (finalProposal.proposedName() == null
                || finalProposal.proposedName().isBlank()) {

            throw new IllegalArgumentException(
                    "Final proposal must include a collection name"
            );
        }

        validateProposal(
                finalProposal.proposedRelationships()
        );

        List<ProposedRelationship> relationships =
                finalProposal
                        .proposedRelationships()
                        .stream()
                        .map(this::mapToEntity)
                        .toList();

        /*
         * Persist EXACTLY what the target owners are going
         * to review.
         *
         * From this point forward:
         *
         * - relationships are frozen
         * - proposed name is frozen
         * - proposed thumbnail is frozen
         */
        mergeRequest.setProposedRelationships(
                relationships
        );

        mergeRequest.setProposedName(
                finalProposal.proposedName().trim()
        );

        mergeRequest.setProposedThumbnailMediaId(
                finalProposal.proposedThumbnailMediaId()
        );

        mergeRequestRepository.save(
                mergeRequest
        );

        /*
         * Only after the COMPLETE final proposal has been
         * persisted do we create the target-owner decisions.
         *
         * Those decisions point back to this MergeRequest,
         * which contains everything the owner is approving:
         *
         * - relationships
         * - proposed collection name
         * - proposed thumbnail
         */
        mergeRequestService.sendRequestToTargetOwners(
                mergeRequestId
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Decision creation
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional
    public void createPendingDecisions(
            UUID mergeRequestId,
            List<CollectionRelationshipResponseDTO> targetOwners
    ) {
        MergeRequest mergeRequest =
                getMergeRequestById(mergeRequestId);

        if (mergeRequest.getStatus()
                != MergeRequestStatus.PENDING) {

            throw new IllegalStateException(
                    "Cannot create decisions for a merge request that is no longer pending"
            );
        }

        if (targetOwners == null || targetOwners.isEmpty()) {
            throw new IllegalArgumentException(
                    "Target collection must have at least one owner"
            );
        }

        /*
         * A finalized proposal must have a name.
         */
        if (mergeRequest.getProposedName() == null
                || mergeRequest.getProposedName().isBlank()) {

            throw new IllegalStateException(
                    "Cannot create decisions before the final proposal has a collection name"
            );
        }

        /*
         * Do not accidentally send the same finalized proposal twice.
         */
        if (!decisionRepository
                .findDecisionByMergeRequestId(
                        mergeRequestId
                )
                .isEmpty()) {

            throw new IllegalStateException(
                    "Merge request has already been sent to target owners"
            );
        }

        /*
         * Create one decision opportunity for every active
         * target owner.
         *
         * These are NOT votes.
         *
         * The first owner to successfully commit a decision
         * determines the outcome of the MergeRequest.
         */
        for (CollectionRelationshipResponseDTO owner : targetOwners) {

            UUID ownerProfileId =
                    owner.profileId();

            MergeRequestDecision decision =
                    MergeRequestDecision.builder()
                            .mergeRequestId(
                                    mergeRequestId
                            )
                            .deciderProfileId(
                                    ownerProfileId
                            )
                            .status(
                                    MergeRequestDecisionStatus.PENDING
                            )
                            .build();

            decisionRepository.save(
                    decision
            );
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Decision reads
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional(readOnly = true)
    public MergeRequestDecisionResponseDTO getDecisionById(UUID decisionId) {
        return mapToResponse(
                getDecisionEntityById(decisionId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestDecisionResponseDTO> getDecisionsByMergeRequestId(
            UUID mergeRequestId
    ) {
        return decisionRepository
                .findDecisionByMergeRequestId(
                        mergeRequestId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestDecisionResponseDTO> getDecisionsByDeciderProfileId(
            UUID profileId
    ) {
        return decisionRepository
                .findByDeciderProfileId(
                        profileId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String getDecisionStatusById(UUID decisionId) {
        return decisionRepository
                .findStatusByMergeRequestDecisionId(
                        decisionId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Merge request decision not found: "
                                        + decisionId
                        )
                );
    }

    /*
     * -------------------------------------------------------------------------
     * Decision actions
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional
    public MergeRequestDecisionResponseDTO approve(
            UUID decisionId,
            UUID profileId
    ) {
        MergeRequestDecision decision =
                getDecisionEntityById(decisionId);

        validateDecider(
                decision,
                profileId
        );

        /*
         * MergeRequestService owns the atomic commit.
         *
         * It locks the parent MergeRequest before deciding
         * whether this owner wins the first-come-first-served
         * race.
         */
        mergeRequestService.commitDecision(
                decision.getMergeRequestId(),
                decisionId,
                profileId,
                MergeRequestDecisionStatus.APPROVED.name()
        );

        return mapToResponse(
                getDecisionEntityById(decisionId)
        );
    }

    @Override
    @Transactional
    public MergeRequestDecisionResponseDTO decline(
            UUID decisionId,
            UUID profileId
    ) {
        MergeRequestDecision decision =
                getDecisionEntityById(decisionId);

        validateDecider(
                decision,
                profileId
        );

        mergeRequestService.commitDecision(
                decision.getMergeRequestId(),
                decisionId,
                profileId,
                MergeRequestDecisionStatus.DECLINED.name()
        );

        return mapToResponse(
                getDecisionEntityById(decisionId)
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Winning decision
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional
    public void winningDecision(
            UUID decisionId,
            UUID profileId,
            MergeRequestDecisionStatus status
    ) {
        MergeRequestDecision decision =
                getDecisionEntityById(decisionId);

        if (!decision
                .getDeciderProfileId()
                .equals(profileId)) {

            throw new IllegalArgumentException(
                    "Profile is not the owner assigned to this merge request decision"
            );
        }

        if (decision.getStatus()
                != MergeRequestDecisionStatus.PENDING) {

            throw new IllegalStateException(
                    "Merge request decision has already been resolved"
            );
        }

        if (status != MergeRequestDecisionStatus.APPROVED
                && status != MergeRequestDecisionStatus.DECLINED) {

            throw new IllegalArgumentException(
                    "Invalid merge request decision status: "
                            + status
            );
        }

        /*
         * MergeRequestService has already locked the parent
         * MergeRequest before calling this method.
         *
         * This method only marks the winning decision.
         */
        decision.setStatus(
                status
        );

        decisionRepository.save(
                decision
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Cancel remaining pending decisions
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional
    public void cancelPendingDecisions(
            UUID mergeRequestId,
            UUID winningDecisionId
    ) {
        /*
         * The winning decision has already been committed.
         *
         * Every other PENDING decision for this MergeRequest
         * is invalidated.
         */
        decisionRepository.cancelPendingDecisions(
                mergeRequestId,
                winningDecisionId
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Validation
     * -------------------------------------------------------------------------
     */

    private void validateRequestor(
            MergeRequest mergeRequest,
            UUID profileId
    ) {
        if (!mergeRequest
                .getRequestedByProfileId()
                .equals(profileId)) {

            throw new IllegalArgumentException(
                    "Profile is not the requestor of this merge request"
            );
        }

        boolean activeOwner =
                collectionRelationshipService
                        .getActiveOwnersByCollectionId(
                                mergeRequest.getSourceCollectionId()
                        )
                        .stream()
                        .anyMatch(owner ->
                                owner.profileId().equals(profileId)
                        );

        if (!activeOwner) {
            throw new IllegalArgumentException(
                    "Requestor is not an active owner of the source collection"
            );
        }
    }

    private void validateDecider(
            MergeRequestDecision decision,
            UUID profileId
    ) {
        if (!decision
                .getDeciderProfileId()
                .equals(profileId)) {

            throw new IllegalArgumentException(
                    "Profile is not the owner assigned to this merge request decision"
            );
        }

        if (decision.getStatus()
                != MergeRequestDecisionStatus.PENDING) {

            throw new IllegalStateException(
                    "Merge request decision has already been resolved"
            );
        }

        MergeRequest mergeRequest =
                getMergeRequestById(
                        decision.getMergeRequestId()
                );

        boolean activeOwner =
                collectionRelationshipService
                        .getActiveOwnersByCollectionId(
                                mergeRequest.getTargetCollectionId()
                        )
                        .stream()
                        .anyMatch(owner ->
                                owner.profileId().equals(profileId)
                        );

        if (!activeOwner) {
            throw new IllegalArgumentException(
                    "Profile is not an active owner of the target collection"
            );
        }
    }

    /*
     * A proposal is editable only while:
     *
     * 1. The MergeRequest itself is still PENDING.
     * 2. It has NOT yet been sent to target owners.
     *
     * The existence of decision records means
     * sendFinalProposal() has already completed.
     */
    private void ensureProposalEditable(
            MergeRequest mergeRequest
    ) {
        if (mergeRequest.getStatus()
                != MergeRequestStatus.PENDING) {

            throw new IllegalStateException(
                    "Merge request proposal can no longer be modified"
            );
        }

        boolean alreadySent =
                !decisionRepository
                        .findDecisionByMergeRequestId(
                                mergeRequest.getMergeRequestId()
                        )
                        .isEmpty();

        if (alreadySent) {
            throw new IllegalStateException(
                    "Merge request proposal has already been finalized and sent to target owners"
            );
        }
    }

    private void validateProposal(
            List<ProposedRelationshipDTO> proposedRelationships
    ) {
        if (proposedRelationships == null) {
            throw new IllegalArgumentException(
                    "Proposed relationships cannot be null"
            );
        }

        if (proposedRelationships.isEmpty()) {
            throw new IllegalArgumentException(
                    "Proposed relationships cannot be empty"
            );
        }

        for (ProposedRelationshipDTO relationship :
                proposedRelationships) {

            if (relationship == null) {
                throw new IllegalArgumentException(
                        "Proposed relationship cannot be null"
                );
            }

            if (relationship.profileId() == null) {
                throw new IllegalArgumentException(
                        "Proposed relationship profile ID cannot be null"
                );
            }

            if (relationship.role() == null) {
                throw new IllegalArgumentException(
                        "Proposed relationship role cannot be null"
                );
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Entity helpers
     * -------------------------------------------------------------------------
     */

    private MergeRequest getMergeRequestById(UUID mergeRequestId) {
        return mergeRequestRepository
                .findMergeRequestById(
                        mergeRequestId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Merge request not found: "
                                        + mergeRequestId
                        )
                );
    }

    private MergeRequestDecision getDecisionEntityById(UUID decisionId) {
        return decisionRepository
                .findDecisionById(
                        decisionId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Merge request decision not found: "
                                        + decisionId
                        )
                );
    }

    /*
     * -------------------------------------------------------------------------
     * Proposal mapping
     * -------------------------------------------------------------------------
     */

    private ProposedRelationship mapToEntity(
            ProposedRelationshipDTO dto
    ) {
        return new ProposedRelationship(
                dto.profileId(),
                dto.role()
        );
    }

    private ProposedRelationshipDTO mapToProposalDTO(
            ProposedRelationship relationship
    ) {
        return new ProposedRelationshipDTO(
                relationship.profileId(),
                relationship.role()
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Response mapping
     * -------------------------------------------------------------------------
     */

    private MergeRequestDecisionResponseDTO mapToResponse(
            MergeRequestDecision decision
    ) {
        return new MergeRequestDecisionResponseDTO(
                decision.getMergeRequestDecisionId(),
                decision.getMergeRequestId(),
                decision.getDeciderProfileId(),
                decision.getStatus(),
                decision.getNote(),
                decision.getRespondedAt()
        );
    }
}
