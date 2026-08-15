package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.ProposedRelationshipDTO;
import com.kinorify.collection.dto.request.CollectionRelationshipRequestDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;

import com.kinorify.collection.dto.response.MergeRequestResponseDTO;
import com.kinorify.collection.entity.Collection;
import com.kinorify.collection.entity.MergeRequest;
import com.kinorify.collection.entity.ProposedRelationship;
import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;
import com.kinorify.collection.entity.enums.MergeRequestStatus;
import com.kinorify.collection.repository.CollectionRepository;
import com.kinorify.collection.repository.MergeRequestRepository;
import com.kinorify.collection.service.CollectionRelationshipService;

import com.kinorify.collection.service.MergeRequestService;
import com.kinorify.collection.service.CollectionMediaService;
import lombok.RequiredArgsConstructor;

import com.kinorify.collection.entity.MergeRequestDecision;
import com.kinorify.collection.repository.MergeRequestDecisionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MergeRequestServiceImpl implements MergeRequestService {

    private final MergeRequestRepository mergeRequestRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionRelationshipService collectionRelationshipService;
    private final MergeRequestDecisionRepository mergeRequestDecisionRepository;
    private final CollectionMediaService collectionMediaService;
    /*
     * -------------------------------------------------------------------------
     * Create merge request
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional
    public MergeRequestResponseDTO createMergeRequest(
            UUID sourceCollectionId,
            UUID targetCollectionId,
            UUID requestedByProfileId
    ) {
        if (sourceCollectionId.equals(targetCollectionId)) {
            throw new IllegalArgumentException(
                    "A collection cannot be merged with itself"
            );
        }

        /*
         * The requestor must be an active owner of the source collection.
         */
        if (!isActiveOwner(
                sourceCollectionId,
                requestedByProfileId
        )) {
            throw new IllegalArgumentException(
                    "Profile is not an active owner of the source collection"
            );
        }

        /*
         * The target collection must have at least one active owner.
         */
        List<CollectionRelationshipResponseDTO> targetOwners =
                collectionRelationshipService
                        .getActiveOwnersByCollectionId(
                                targetCollectionId
                        );

        if (targetOwners.isEmpty()) {
            throw new IllegalArgumentException(
                    "Target collection has no active owners"
            );
        }

        /*
         * Only one PENDING merge request may exist between
         * these two collections at a time.
         */
        if (mergeRequestRepository
                .findPendingMergeRequest(
                        sourceCollectionId,
                        targetCollectionId
                )
                .isPresent()) {

            throw new IllegalStateException(
                    "A pending merge request already exists between these collections"
            );
        }

        /*
         * Gather the INITIAL relationship proposal.
         *
         * CollectionRelationshipResponseDTO represents existing
         * relationships. MergeRequest stores only the proposed
         * profile/role combination.
         */
        List<UUID> collectionIds = List.of(
                sourceCollectionId,
                targetCollectionId
        );

        List<ProposedRelationship> proposedRelationships =
                collectionRelationshipService
                        .getProposedRelationshipsByCollectionIds(
                                collectionIds
                        )
                        .stream()
                        .map(relationship ->
                                new ProposedRelationship(
                                        relationship.profileId(),
                                        relationship.role()
                                )
                        )
                        .toList();

        /*
         * Name and thumbnail are intentionally not supplied here.
         *
         * The requestor supplies those while editing the proposal
         * before sendFinalProposal().
         */
        MergeRequest mergeRequest =
                MergeRequest.builder()
                        .sourceCollectionId(sourceCollectionId)
                        .targetCollectionId(targetCollectionId)
                        .requestedByProfileId(requestedByProfileId)
                        .proposedRelationships(proposedRelationships)
                        .status(MergeRequestStatus.PENDING)
                        .build();

        MergeRequest savedRequest =
                mergeRequestRepository.save(mergeRequest);

        return mapToResponse(savedRequest);
    }

    /*
     * -------------------------------------------------------------------------
     * Send finalized request to target owners
     * -------------------------------------------------------------------------
     */

@Override
@Transactional
public void sendRequestToTargetOwners(UUID mergeRequestId) {
    MergeRequest mergeRequest =
            getEntityById(mergeRequestId);

    if (mergeRequest.getStatus() != MergeRequestStatus.PENDING) {
        throw new IllegalStateException(
                "Merge request is no longer pending"
        );
    }

    if (mergeRequest.getProposedName() == null
            || mergeRequest.getProposedName().isBlank()) {

        throw new IllegalStateException(
                "A finalized merge proposal must include a collection name"
        );
    }

    if (mergeRequest.getProposedRelationships() == null
            || mergeRequest.getProposedRelationships().isEmpty()) {

        throw new IllegalStateException(
                "A finalized merge proposal must include proposed relationships"
        );
    }

    List<CollectionRelationshipResponseDTO> targetOwners =
            collectionRelationshipService
                    .getActiveOwnersByCollectionId(
                            mergeRequest.getTargetCollectionId()
                    );

    if (targetOwners.isEmpty()) {
        throw new IllegalStateException(
                "Target collection has no active owners"
        );
    }

    /*
     * Prevent the finalized proposal from being sent twice.
     */
    if (!mergeRequestDecisionRepository
            .findDecisionByMergeRequestId(
                    mergeRequestId
            )
            .isEmpty()) {

        throw new IllegalStateException(
                "Merge request has already been sent to target owners"
        );
    }

    /*
     * Create one PENDING decision opportunity for every
     * active target owner.
     *
     * These are not votes. The first owner to commit wins.
     */
    for (CollectionRelationshipResponseDTO owner : targetOwners) {

        MergeRequestDecision decision =
                MergeRequestDecision.builder()
                        .mergeRequestId(
                                mergeRequestId
                        )
                        .deciderProfileId(
                                owner.profileId()
                        )
                        .status(
                                MergeRequestDecisionStatus.PENDING
                        )
                        .build();

        mergeRequestDecisionRepository.save(
                decision
        );
    }
}
    /*
     * -------------------------------------------------------------------------
     * Commit decision
     * -------------------------------------------------------------------------
     */

@Override
@Transactional
public void commitDecision(
        UUID mergeRequestId,
        UUID decisionId,
        UUID profileId,
        String status
) {
    MergeRequestDecisionStatus decisionStatus;

    try {
        decisionStatus =
                MergeRequestDecisionStatus.valueOf(
                        status
                );
    } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException(
                "Invalid merge request decision status: "
                        + status
        );
    }

    if (decisionStatus
            != MergeRequestDecisionStatus.APPROVED
            && decisionStatus
            != MergeRequestDecisionStatus.DECLINED) {

        throw new IllegalArgumentException(
                "Invalid merge request decision status: "
                        + status
        );
    }

    MergeRequestStatus mergeRequestStatus =
            decisionStatus
                    == MergeRequestDecisionStatus.APPROVED
                    ? MergeRequestStatus.APPROVED
                    : MergeRequestStatus.DECLINED;

    /*
     * Lock the MergeRequest row.
     *
     * This is the first-come-first-served synchronization
     * point for all target owners.
     */
    MergeRequest mergeRequest =
            mergeRequestRepository
                    .findMergeRequestByIdForUpdate(
                            mergeRequestId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Merge request not found: "
                                            + mergeRequestId
                            )
                    );

    /*
     * Another owner already committed this request.
     */
    if (mergeRequest.getStatus()
            != MergeRequestStatus.PENDING) {

        UUID committedByProfileId =
                getCommittedByProfileId(
                        mergeRequestId
                );

        throw new IllegalStateException(
                "status has already been committed by:"
                        + committedByProfileId
                        + " to status: "
                        + mergeRequest.getStatus()
        );
    }

    MergeRequestDecision winningDecision =
            mergeRequestDecisionRepository
                    .findDecisionById(
                            decisionId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Merge request decision not found: "
                                            + decisionId
                            )
                    );

    /*
     * Make sure the decision actually belongs to this
     * MergeRequest.
     */
    if (!winningDecision
            .getMergeRequestId()
            .equals(mergeRequestId)) {

        throw new IllegalArgumentException(
                "Decision does not belong to this merge request"
        );
    }

    /*
     * Make sure the authenticated profile owns this
     * decision opportunity.
     */
    if (!winningDecision
            .getDeciderProfileId()
            .equals(profileId)) {

        throw new IllegalArgumentException(
                "Profile is not the owner assigned to this merge request decision"
        );
    }

    if (winningDecision.getStatus()
            != MergeRequestDecisionStatus.PENDING) {

        throw new IllegalStateException(
                "Merge request decision has already been resolved"
        );
    }

    /*
     * Commit the winning decision.
     */
    winningDecision.setStatus(
            decisionStatus
    );

    mergeRequestDecisionRepository.save(
            winningDecision
    );

    /*
     * Cancel every other PENDING decision.
     */
    killOtherPendingForThatMR(
            mergeRequestId,
            decisionId
    );

    /*
     * Commit the overall MergeRequest outcome.
     */
    mergeRequest.setStatus(
            mergeRequestStatus
    );

    mergeRequest.setRespondedAt(
            OffsetDateTime.now()
    );

    MergeRequest savedRequest =
            mergeRequestRepository.save(
                    mergeRequest
            );

    /*
     * An approved final proposal becomes a new collection.
     */
    if (mergeRequestStatus
            == MergeRequestStatus.APPROVED) {

        createNewMergedCollection(
                savedRequest.getMergeRequestId()
        );
    }
}

    /*
     * -------------------------------------------------------------------------
     * Kill remaining pending decisions
     * -------------------------------------------------------------------------
     */

@Override
@Transactional
public void killOtherPendingForThatMR(
        UUID mergeRequestId,
        UUID winningDecisionId
) {
    mergeRequestDecisionRepository
            .cancelPendingDecisions(
                    mergeRequestId,
                    winningDecisionId
            );
}

    /*
     * -------------------------------------------------------------------------
     * Create new merged collection
     * -------------------------------------------------------------------------
     */

@Override
@Transactional
public UUID createNewMergedCollection(UUID mergeRequestId) {
    MergeRequest mergeRequest =
            getEntityById(mergeRequestId);

    if (mergeRequest.getStatus()
            != MergeRequestStatus.APPROVED) {

        throw new IllegalStateException(
                "Cannot create a merged collection from a merge request that is not APPROVED"
        );
    }

    /*
     * Prevent accidental duplicate creation if this operation
     * is somehow invoked more than once.
     */
    if (mergeRequest.getNewCollectionId() != null) {
        throw new IllegalStateException(
                "A merged collection has already been created for merge request: "
                        + mergeRequestId
        );
    }

    if (mergeRequest.getProposedName() == null
            || mergeRequest.getProposedName().isBlank()) {

        throw new IllegalStateException(
                "Approved merge request does not contain a proposed collection name"
        );
    }

    if (mergeRequest.getProposedRelationships() == null
            || mergeRequest.getProposedRelationships().isEmpty()) {

        throw new IllegalStateException(
                "Approved merge request does not contain proposed relationships"
        );
    }

    /*
     * Create the new collection using exactly the name and
     * optional thumbnail that were included in the approved
     * final proposal.
     */
    Collection collection =
            Collection.builder()
                    .name(
                            mergeRequest.getProposedName()
                    )
                    .thumbnailMediaId(
                            mergeRequest.getProposedThumbnailMediaId()
                    )
                    .createdByProfileId(
                            mergeRequest.getRequestedByProfileId()
                    )
                    .build();

    Collection savedCollection =
            collectionRepository.save(
                    collection
            );

    /*
     * Materialize every relationship from the FINAL proposal.
     *
     * Merge relationships are created directly as ACTIVE because
     * the proposal has already been approved.
     */
    for (ProposedRelationship relationship :
            mergeRequest.getProposedRelationships()) {

        CollectionRelationshipRequestDTO request =
                new CollectionRelationshipRequestDTO(
                        relationship.profileId(),
                        relationship.role()
                );

        collectionRelationshipService
                .createMergeRelationships(
                        savedCollection.getCollectionId(),
                        request,
                        mergeRequest.getRequestedByProfileId()
                );
    }

    /*
     * Combine the active media belonging to the source and target
     * collections and attach that media to the new merged collection.
     *
     * CollectionMediaService owns all media/collection orchestration,
     * including duplicate removal and creation of CollectionMedia rows.
     */
    collectionMediaService.addMediaToMergedCollection(
            savedCollection.getCollectionId(),
            mergeRequest.getSourceCollectionId(),
            mergeRequest.getTargetCollectionId(),
            mergeRequest.getRequestedByProfileId()
    );

    /*
     * Preserve the resulting collection ID on the MergeRequest
     * for history and future lineage tracking.
     */
    mergeRequest.setNewCollectionId(
            savedCollection.getCollectionId()
    );

    mergeRequestRepository.save(
            mergeRequest
    );

    return savedCollection.getCollectionId();
}

    /*
     * -------------------------------------------------------------------------
     * Read methods
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional(readOnly = true)
    public MergeRequestResponseDTO getMergeRequestById(
            UUID mergeRequestId
    ) {
        return mapToResponse(
                getEntityById(mergeRequestId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestResponseDTO>
    getMergeRequestsByRequestedByProfileId(
            UUID profileId
    ) {
        return mergeRequestRepository
                .findMergeRequestsByRequestedByProfileId(
                        profileId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestResponseDTO>
    getPendingMergeRequestsByCollectionId(
            UUID collectionId
    ) {
        return mergeRequestRepository
                .findPendingMergeRequestsByCollectionId(
                        collectionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestResponseDTO>
    getMergeRequestsByStatus(
            String status
    ) {
        return mergeRequestRepository
                .findMergeRequestsByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestResponseDTO>
    getMergeRequestsByTargetCollectionId(
            UUID collectionId
    ) {
        return mergeRequestRepository
                .findAllMergeRequestByTargetCollectionId(
                        collectionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MergeRequestResponseDTO>
    getMergeRequestsBySourceCollectionId(
            UUID collectionId
    ) {
        return mergeRequestRepository
                .findAllMergeRequestBySourceCollectionId(
                        collectionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPendingMergeRequestByCollectionId(
            UUID collectionId
    ) {
        return mergeRequestRepository
                .existsPendingMergeRequestByCollectionId(
                        collectionId
                );
    }

    /*
     * -------------------------------------------------------------------------
     * Helpers
     * -------------------------------------------------------------------------
     */

    private MergeRequest getEntityById(
            UUID mergeRequestId
    ) {
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

    private boolean isActiveOwner(
            UUID collectionId,
            UUID profileId
    ) {
        return collectionRelationshipService
                .getActiveOwnersByCollectionId(
                        collectionId
                )
                .stream()
                .anyMatch(owner ->
                        owner.profileId().equals(profileId)
                );
    }

    /*
     * The MergeRequest entity does not currently persist the
     * committing profile ID.
     *
     * The winning decision already preserves that information,
     * so retrieve it from the resolved decisions when needed
     * for the "already committed" error.
     */
    private UUID getCommittedByProfileId(UUID mergeRequestId) {
    return mergeRequestDecisionRepository
            .findDecisionByMergeRequestId(
                    mergeRequestId
            )
            .stream()
            .filter(decision ->
                    decision.getStatus()
                            == MergeRequestDecisionStatus.APPROVED
                            || decision.getStatus()
                            == MergeRequestDecisionStatus.DECLINED
            )
            .map(
                    MergeRequestDecision::getDeciderProfileId
            )
            .findFirst()
            .orElse(null);
}

    /*
     * -------------------------------------------------------------------------
     * Response mapping
     * -------------------------------------------------------------------------
     */

    private MergeRequestResponseDTO mapToResponse(
            MergeRequest mergeRequest
    ) {
        List<ProposedRelationshipDTO> proposedRelationships =
                mergeRequest
                        .getProposedRelationships()
                        .stream()
                        .map(relationship ->
                                new ProposedRelationshipDTO(
                                        relationship.profileId(),
                                        relationship.role()
                                )
                        )
                        .toList();

        return new MergeRequestResponseDTO(
                mergeRequest.getMergeRequestId(),
                mergeRequest.getSourceCollectionId(),
                mergeRequest.getTargetCollectionId(),
                mergeRequest.getRequestedByProfileId(),
                mergeRequest.getNewCollectionId(),
                mergeRequest.getStatus(),
                proposedRelationships,
                mergeRequest.getProposedName(),
                mergeRequest.getProposedThumbnailMediaId(),
                mergeRequest.getRequestedAt(),
                mergeRequest.getRespondedAt()
        );
    }
}
