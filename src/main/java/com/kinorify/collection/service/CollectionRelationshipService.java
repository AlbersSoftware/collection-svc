package com.kinorify.collection.service;

import com.kinorify.collection.dto.request.CollectionRelationshipRequestDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.entity.enums.CollectionRelationshipStatus;

import java.util.List;
import java.util.UUID;

public interface CollectionRelationshipService {

    CollectionRelationshipResponseDTO createOwnerRelationship(UUID collectionId, UUID profileId);

    CollectionRelationshipResponseDTO createRelationship(UUID collectionId, CollectionRelationshipRequestDTO request, UUID invitedByProfileId);

    CollectionRelationshipResponseDTO getRelationshipById(UUID relationshipId);

    CollectionRelationshipResponseDTO getRelationshipByCollectionIdAndProfileId(UUID collectionId, UUID profileId);

    List<CollectionRelationshipResponseDTO> getRelationshipsByCollectionId(UUID collectionId);

    List<CollectionRelationshipResponseDTO> getActiveOwnersByCollectionId(UUID collectionId);

    CollectionRelationshipResponseDTO getActiveOwnerByCollectionIdAndProfileId(UUID collectionId, UUID profileId);

    List<CollectionRelationshipResponseDTO> getActiveRelationshipsByProfileId(UUID profileId);

    List<CollectionRelationshipResponseDTO> getActiveRelationshipsByCollectionIds(List<UUID> collectionIds);

    List<CollectionRelationshipResponseDTO> getRelationshipsByRole(CollectionRelationshipRole role);

    List<CollectionRelationshipResponseDTO> getRelationshipsByStatus(CollectionRelationshipStatus status);

    List<CollectionRelationshipResponseDTO> getPendingRelationshipsByCollectionIds(List<UUID> collectionIds);

    List<CollectionRelationshipResponseDTO> getDeclinedRelationshipsByCollectionIds(List<UUID> collectionIds);

    List<CollectionRelationshipResponseDTO> getRevokedRelationshipsByCollectionIds(List<UUID> collectionIds);

    List<CollectionRelationshipResponseDTO> getRelationshipsInvitedByProfileIdByCollectionId(UUID profileId, UUID collectionId);

    List<String> getStatusesByCollectionId(UUID collectionId);

    List<CollectionRelationshipResponseDTO> getRelationshipsForProfileId(UUID profileId);

    CollectionRelationshipResponseDTO acceptRelationship(UUID relationshipId, UUID profileId);

    CollectionRelationshipResponseDTO declineRelationship(UUID relationshipId, UUID profileId);

    CollectionRelationshipResponseDTO revokeRelationship(UUID relationshipId, UUID profileId);

    CollectionRelationshipResponseDTO updateRelationshipRole(UUID relationshipId, UUID profileId, CollectionRelationshipRole role);

    CollectionRelationshipResponseDTO createMergeRelationships(UUID collectionId, CollectionRelationshipRequestDTO request, UUID invitedByProfileId);
    
    List<CollectionRelationshipResponseDTO> getPendingRelationshipsByProfileId(UUID profileId);

    List<CollectionRelationshipResponseDTO> getRelationshipsInvitedByProfileId(UUID profileId);


    /*
 * WARNING:
 * This method bypasses the normal relationship status business rules.
 * Do NOT expose or use this method in production application flows.
 * It is intended only for administrative, testing, migration, or other
 * controlled internal operations.
 */
CollectionRelationshipResponseDTO updateRelationshipStatus(UUID relationshipId, CollectionRelationshipStatus status);

    List<CollectionRelationshipResponseDTO> getProposedRelationshipsByCollectionIds(List<UUID> collectionIds);
}
