package com.kinorify.collection.rest;

import com.kinorify.collection.config.JwtService;
import com.kinorify.collection.dto.request.CollectionRelationshipRequestDTO;
import com.kinorify.collection.dto.request.CollectionRequestDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.dto.response.CollectionResponseDTO;
import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.service.CollectionRelationshipService;
import com.kinorify.collection.service.CollectionService;
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
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final CollectionRelationshipService collectionRelationshipService;
    private final ProfileClientService profileClientService;
    private final JwtService jwtService;

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    @PostMapping
    public ResponseEntity<CollectionResponseDTO> createCollection(
            @Valid @RequestBody CollectionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionResponseDTO createdCollection =
                collectionService.createCollection(
                        request,
                        profileId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCollection);
    }

    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionResponseDTO> updateCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CollectionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionResponseDTO updatedCollection =
                collectionService.updateCollection(
                        collectionId,
                        request,
                        profileId
                );

        return ResponseEntity.ok(
                updatedCollection
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Reads
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionResponseDTO> getCollectionById(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        CollectionResponseDTO collection =
                collectionService.getCollectionById(
                        collectionId
                );

        return ResponseEntity.ok(
                collection
        );
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByName(
            @PathVariable String name,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionResponseDTO> collections =
                collectionService.getCollectionByName(
                        name
                );

        return ResponseEntity.ok(
                collections
        );
    }

    /*
     * Returns collections originally created by the currently
     * authenticated profile.
     */
    @GetMapping("/created-by-me")
    public ResponseEntity<List<CollectionResponseDTO>>
    getCollectionsCreatedByCurrentProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        List<CollectionResponseDTO> collections =
                collectionService
                        .getCollectionsCreatedByProfileId(
                                profileId
                        );

        return ResponseEntity.ok(
                collections
        );
    }

    /*
     * Returns collections where the currently authenticated
     * profile has an active relationship.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<CollectionResponseDTO>>
    getCollectionsForCurrentProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        List<CollectionResponseDTO> collections =
                collectionService
                        .getCollectionsByProfileId(
                                profileId
                        );

        return ResponseEntity.ok(
                collections
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Delete
     * -------------------------------------------------------------------------
     */

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        collectionService.deleteCollection(
                collectionId,
                profileId
        );

        return ResponseEntity.noContent().build();
    }

    // relationship service endpoints

    /*
     * -------------------------------------------------------------------------
     * Create relationship invitation
     * -------------------------------------------------------------------------
     *
     * Creates a PENDING relationship.
     *
     * The authenticated profile becomes the invitedByProfileId.
     * The service/controller workflow can later be tightened so
     * only active owners are allowed to create invitations.
     * -------------------------------------------------------------------------
     */

    @PostMapping("/{collectionId}/relationships")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    createRelationship(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CollectionRelationshipRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService.createRelationship(
                        collectionId,
                        request,
                        profileId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(relationship);
    }

    /*
     * -------------------------------------------------------------------------
     * Relationship reads
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{collectionId}/relationships")
    public ResponseEntity<List<CollectionRelationshipResponseDTO>>
    getRelationshipsByCollectionId(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionRelationshipResponseDTO> relationships =
                collectionRelationshipService
                        .getRelationshipsByCollectionId(
                                collectionId
                        );

        return ResponseEntity.ok(
                relationships
        );
    }

    @GetMapping("/{collectionId}/relationships/{relationshipId}")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    getRelationshipById(
            @PathVariable UUID collectionId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .getRelationshipById(
                                relationshipId
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    @GetMapping("/{collectionId}/relationships/me")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    getCurrentProfilesRelationship(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .getRelationshipByCollectionIdAndProfileId(
                                collectionId,
                                profileId
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    @GetMapping("/{collectionId}/owners")
    public ResponseEntity<List<CollectionRelationshipResponseDTO>>
    getActiveOwners(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionRelationshipResponseDTO> owners =
                collectionRelationshipService
                        .getActiveOwnersByCollectionId(
                                collectionId
                        );

        return ResponseEntity.ok(
                owners
        );
    }


    @GetMapping("/relationships/incoming")
    public ResponseEntity<List<CollectionRelationshipResponseDTO>>
    getIncomingRelationships(
        @AuthenticationPrincipal Jwt jwt
      ) {
    UUID profileId =
            getCurrentProfileId(jwt);

    return ResponseEntity.ok(
            collectionRelationshipService
                    .getPendingRelationshipsByProfileId(
                            profileId
                    )
    );
}

    @GetMapping("/relationships/outgoing")
    public ResponseEntity<List<CollectionRelationshipResponseDTO>>
    getOutgoingRelationships(
        @AuthenticationPrincipal Jwt jwt
    ) {
    UUID profileId =
            getCurrentProfileId(jwt);

    return ResponseEntity.ok(
            collectionRelationshipService
                    .getRelationshipsInvitedByProfileId(
                            profileId
                    )
    );
}

    /*
     * -------------------------------------------------------------------------
     * Accept relationship invitation
     * -------------------------------------------------------------------------
     *
     * Only the profile represented by the relationship may accept it.
     * The service also requires the relationship to currently be PENDING.
     * -------------------------------------------------------------------------
     */

    @PutMapping("/{collectionId}/relationships/{relationshipId}/accept")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    acceptRelationship(
            @PathVariable UUID collectionId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .acceptRelationship(
                                relationshipId,
                                profileId
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Decline relationship invitation
     * -------------------------------------------------------------------------
     */

    @PutMapping("/{collectionId}/relationships/{relationshipId}/decline")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    declineRelationship(
            @PathVariable UUID collectionId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .declineRelationship(
                                relationshipId,
                                profileId
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Revoke relationship
     * -------------------------------------------------------------------------
     *
     * Non-owner relationships may be revoked by an active owner.
     *
     * OWNER relationships follow the special service rules:
     * - an owner cannot revoke another owner
     * - an owner may voluntarily give up ownership
     * - the final owner cannot give up ownership
     * -------------------------------------------------------------------------
     */

    @PutMapping("/{collectionId}/relationships/{relationshipId}/revoke")
    public ResponseEntity<CollectionRelationshipResponseDTO>
    revokeRelationship(
            @PathVariable UUID collectionId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .revokeRelationship(
                                relationshipId,
                                profileId
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Update relationship role
     * -------------------------------------------------------------------------
     *
     * Role-change business rules remain inside
     * CollectionRelationshipService.
     * -------------------------------------------------------------------------
     */

    @PutMapping(
            "/{collectionId}/relationships/{relationshipId}/role/{role}"
    )
    public ResponseEntity<CollectionRelationshipResponseDTO>
    updateRelationshipRole(
            @PathVariable UUID collectionId,
            @PathVariable UUID relationshipId,
            @PathVariable CollectionRelationshipRole role,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .updateRelationshipRole(
                                relationshipId,
                                profileId,
                                role
                        );

        return ResponseEntity.ok(
                relationship
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Current profile relationships
     * -------------------------------------------------------------------------
     *
     * Useful for testing and eventually for a memberships/sharing screen.
     * -------------------------------------------------------------------------
     */

    @GetMapping("/relationships/mine")
    public ResponseEntity<List<CollectionRelationshipResponseDTO>>
    getCurrentProfilesRelationships(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        List<CollectionRelationshipResponseDTO> relationships =
                collectionRelationshipService
                        .getRelationshipsForProfileId(
                                profileId
                        );

        return ResponseEntity.ok(
                relationships
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
