package com.kinorify.collection.rest;

import com.kinorify.collection.config.JwtService;
import com.kinorify.collection.dto.FinalProposalDTO;
import com.kinorify.collection.dto.MergeRequestDTO;
import com.kinorify.collection.dto.ProposedRelationshipDTO;
import com.kinorify.collection.dto.response.MergeRequestDecisionResponseDTO;
import com.kinorify.collection.dto.response.MergeRequestResponseDTO;
import com.kinorify.collection.service.CollectionMediaService;
import com.kinorify.collection.service.MergeRequestDecisionService;
import com.kinorify.collection.service.MergeRequestService;
import com.kinorify.collection.service.ProfileClientService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merge-requests")
@RequiredArgsConstructor
public class MergeRequestController {

    private final MergeRequestService mergeRequestService;
    private final MergeRequestDecisionService mergeRequestDecisionService;
    private final CollectionMediaService collectionMediaService;

    private final ProfileClientService profileClientService;
    private final JwtService jwtService;

    /*
     * -------------------------------------------------------------------------
     * Create merge request
     * -------------------------------------------------------------------------
     */

    @PostMapping("/source/{sourceCollectionId}")
    public ResponseEntity<MergeRequestResponseDTO> createMergeRequest(
            @PathVariable UUID sourceCollectionId,
            @Valid @RequestBody MergeRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        MergeRequestResponseDTO created =
                mergeRequestService.createMergeRequest(
                        sourceCollectionId,
                        request.targetCollectionId(),
                        profileId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /*
     * -------------------------------------------------------------------------
     * Proposal editing
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{mergeRequestId}/proposal")
    public ResponseEntity<List<ProposedRelationshipDTO>> getProposal(
            @PathVariable UUID mergeRequestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestDecisionService.getProposal(
                        mergeRequestId
                )
        );
    }

    @PutMapping("/{mergeRequestId}/proposal/relationships")
    public ResponseEntity<List<ProposedRelationshipDTO>> updateProposal(
            @PathVariable UUID mergeRequestId,
            @Valid @RequestBody List<ProposedRelationshipDTO> proposedRelationships,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        List<ProposedRelationshipDTO> updatedProposal =
                mergeRequestDecisionService.updateProposal(
                        mergeRequestId,
                        profileId,
                        proposedRelationships
                );

        return ResponseEntity.ok(
                updatedProposal
        );
    }

    @PutMapping("/{mergeRequestId}/proposal/details")
    public ResponseEntity<Void> supplyNameAndThumbnail(
            @PathVariable UUID mergeRequestId,
            @RequestParam String proposedName,
            @RequestParam(required = false) UUID proposedThumbnailMediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        mergeRequestDecisionService.supplyNameAndThumbnail(
                mergeRequestId,
                profileId,
                proposedName,
                proposedThumbnailMediaId
        );

        return ResponseEntity.noContent().build();
    }

    /*
     * -------------------------------------------------------------------------
     * Media preview
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{mergeRequestId}/proposal/media")
    public ResponseEntity<List<UUID>> getCombinedMediaPreview(
            @PathVariable UUID mergeRequestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        MergeRequestResponseDTO mergeRequest =
                mergeRequestService.getMergeRequestById(
                        mergeRequestId
                );

        List<UUID> mediaIds =
                collectionMediaService.combineMediaForMerge(
                        mergeRequest.sourceCollectionId(),
                        mergeRequest.targetCollectionId()
                );

        return ResponseEntity.ok(
                mediaIds
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Finalize proposal
     * -------------------------------------------------------------------------
     */

    @PostMapping("/{mergeRequestId}/proposal/finalize")
    public ResponseEntity<Void> sendFinalProposal(
            @PathVariable UUID mergeRequestId,
            @Valid @RequestBody FinalProposalDTO finalProposal,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        mergeRequestDecisionService.sendFinalProposal(
                mergeRequestId,
                profileId,
                finalProposal
        );

        return ResponseEntity.noContent().build();
    }

    /*
     * -------------------------------------------------------------------------
     * Decision actions
     * -------------------------------------------------------------------------
     */

    @PutMapping("/decisions/{decisionId}/approve")
    public ResponseEntity<MergeRequestDecisionResponseDTO> approve(
            @PathVariable UUID decisionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        MergeRequestDecisionResponseDTO response =
                mergeRequestDecisionService.approve(
                        decisionId,
                        profileId
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PutMapping("/decisions/{decisionId}/decline")
    public ResponseEntity<MergeRequestDecisionResponseDTO> decline(
            @PathVariable UUID decisionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        MergeRequestDecisionResponseDTO response =
                mergeRequestDecisionService.decline(
                        decisionId,
                        profileId
                );

        return ResponseEntity.ok(
                response
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Merge request reads
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{mergeRequestId}")
    public ResponseEntity<MergeRequestResponseDTO> getMergeRequestById(
            @PathVariable UUID mergeRequestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestService.getMergeRequestById(
                        mergeRequestId
                )
        );
    }

    @GetMapping("/requested-by-me")
    public ResponseEntity<List<MergeRequestResponseDTO>>
    getMergeRequestsRequestedByCurrentProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestService
                        .getMergeRequestsByRequestedByProfileId(
                                profileId
                        )
        );
    }

    @GetMapping("/collection/{collectionId}/pending")
    public ResponseEntity<List<MergeRequestResponseDTO>>
    getPendingMergeRequestsByCollectionId(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestService
                        .getPendingMergeRequestsByCollectionId(
                                collectionId
                        )
        );
    }

    @GetMapping("/collection/{collectionId}/target")
    public ResponseEntity<List<MergeRequestResponseDTO>>
    getMergeRequestsByTargetCollectionId(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestService
                        .getMergeRequestsByTargetCollectionId(
                                collectionId
                        )
        );
    }

    @GetMapping("/collection/{collectionId}/source")
    public ResponseEntity<List<MergeRequestResponseDTO>>
    getMergeRequestsBySourceCollectionId(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestService
                        .getMergeRequestsBySourceCollectionId(
                                collectionId
                        )
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Decision reads
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{mergeRequestId}/decisions")
    public ResponseEntity<List<MergeRequestDecisionResponseDTO>>
    getDecisionsByMergeRequestId(
            @PathVariable UUID mergeRequestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestDecisionService
                        .getDecisionsByMergeRequestId(
                                mergeRequestId
                        )
        );
    }

    @GetMapping("/decisions/mine")
    public ResponseEntity<List<MergeRequestDecisionResponseDTO>>
    getCurrentProfilesDecisions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        return ResponseEntity.ok(
                mergeRequestDecisionService
                        .getDecisionsByDeciderProfileId(
                                profileId
                        )
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Current profile resolution
     * -------------------------------------------------------------------------
     */

    private UUID getCurrentProfileId(Jwt jwt) {
        String cognitoSub =
                jwtService.getCognitoSub(jwt);

        return profileClientService
                .getProfileIdByCognitoSub(
                        cognitoSub
                );
    }
}
