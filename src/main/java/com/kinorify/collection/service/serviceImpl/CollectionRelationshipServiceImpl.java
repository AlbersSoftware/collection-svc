package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.request.CollectionRelationshipRequestDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.entity.CollectionRelationship;
import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.entity.enums.CollectionRelationshipStatus;
import com.kinorify.collection.repository.CollectionRelationshipRepository;
import com.kinorify.collection.repository.projection.ProposedRelationshipProjection;
import com.kinorify.collection.service.CollectionRelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionRelationshipServiceImpl implements CollectionRelationshipService {

    private final CollectionRelationshipRepository collectionRelationshipRepository;

    @Override
    public CollectionRelationshipResponseDTO createOwnerRelationship(UUID collectionId, UUID profileId) {
        CollectionRelationship relationship = CollectionRelationship.builder()
                .collectionId(collectionId)
                .profileId(profileId)
                .role(CollectionRelationshipRole.OWNER)
                .status(CollectionRelationshipStatus.ACTIVE)
                .invitedByProfileId(null)
                .build();

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }

    @Override
    public CollectionRelationshipResponseDTO createRelationship(UUID collectionId, CollectionRelationshipRequestDTO request, UUID invitedByProfileId) {
        CollectionRelationship relationship = CollectionRelationship.builder()
                .collectionId(collectionId)
                .profileId(request.profileId())
                .role(request.role())
                .status(CollectionRelationshipStatus.PENDING)
                .invitedByProfileId(invitedByProfileId)
                .build();

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }


@Override
public CollectionRelationshipResponseDTO createMergeRelationships(
        UUID collectionId,
        CollectionRelationshipRequestDTO request,
        UUID invitedByProfileId
) {
    CollectionRelationship relationship =
            CollectionRelationship.builder()
                    .collectionId(collectionId)
                    .profileId(request.profileId())
                    .role(request.role())
                    .status(CollectionRelationshipStatus.ACTIVE)
                    .invitedByProfileId(invitedByProfileId)
                    .build();

    return mapToResponse(
            collectionRelationshipRepository.save(relationship)
    );
}

    @Override
    @Transactional(readOnly = true)
    public CollectionRelationshipResponseDTO getRelationshipById(UUID relationshipId) {
        return collectionRelationshipRepository.findRelationshipById(relationshipId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collection relationship not found: " + relationshipId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionRelationshipResponseDTO getRelationshipByCollectionIdAndProfileId(UUID collectionId, UUID profileId) {
        return collectionRelationshipRepository
                .findRelationshipByCollectionIdAndProfileId(collectionId, profileId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collection relationship not found for collection " + collectionId +
                        " and profile " + profileId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRelationshipsByCollectionId(UUID collectionId) {
        return collectionRelationshipRepository.findRelationshipsByCollectionId(collectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getActiveOwnersByCollectionId(UUID collectionId) {
        return collectionRelationshipRepository.findActiveOwnersByCollectionId(collectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionRelationshipResponseDTO getActiveOwnerByCollectionIdAndProfileId(UUID collectionId, UUID profileId) {
        return collectionRelationshipRepository
                .findActiveOwnerByCollectionIdAndProfileId(collectionId, profileId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active owner relationship not found for collection " + collectionId +
                        " and profile " + profileId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getActiveRelationshipsByProfileId(UUID profileId) {
        return collectionRelationshipRepository.findActiveRelationshipsByProfileId(profileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getActiveRelationshipsByCollectionIds(List<UUID> collectionIds) {
        return collectionRelationshipRepository.findActiveRelationshipsByCollectionIds(collectionIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRelationshipsByRole(CollectionRelationshipRole role) {
        return collectionRelationshipRepository.findRelationshipsByRole(role.name())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRelationshipsByStatus(CollectionRelationshipStatus status) {
        return collectionRelationshipRepository.findRelationshipsByStatus(status.name())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getPendingRelationshipsByCollectionIds(List<UUID> collectionIds) {
        return collectionRelationshipRepository.findPendingRelationshipsByCollectionIds(collectionIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getDeclinedRelationshipsByCollectionIds(List<UUID> collectionIds) {
        return collectionRelationshipRepository.findDeclinedRelationshipsByCollectionIds(collectionIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRevokedRelationshipsByCollectionIds(List<UUID> collectionIds) {
        return collectionRelationshipRepository.findRevokedRelationshipsByCollectionIds(collectionIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRelationshipsInvitedByProfileIdByCollectionId(UUID profileId, UUID collectionId) {
        return collectionRelationshipRepository
                .findRelationshipsInvitedByProfileIdByCollectionId(profileId, collectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getStatusesByCollectionId(UUID collectionId) {
        return collectionRelationshipRepository.findStatusesByCollectionId(collectionId);
    }
// redundant method until other statuses involved e.g revoked/declined/pending.
    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getRelationshipsForProfileId(UUID profileId) {
        return collectionRelationshipRepository.findActiveRelationshipsByProfileId(profileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
// status methods based on rules
    @Override
    public CollectionRelationshipResponseDTO acceptRelationship(UUID relationshipId, UUID profileId) {
        CollectionRelationship relationship = getEntityById(relationshipId);

        if (!relationship.getProfileId().equals(profileId)) {
            throw new IllegalStateException(
                    "Only the invited profile can accept this relationship."
            );
        }

        if (relationship.getStatus() != CollectionRelationshipStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending relationship can be accepted."
            );
        }

        relationship.setStatus(CollectionRelationshipStatus.ACTIVE);

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }

    @Override
    public CollectionRelationshipResponseDTO declineRelationship(UUID relationshipId, UUID profileId) {
        CollectionRelationship relationship = getEntityById(relationshipId);

        if (!relationship.getProfileId().equals(profileId)) {
            throw new IllegalStateException(
                    "Only the invited profile can decline this relationship."
            );
        }

        if (relationship.getStatus() != CollectionRelationshipStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending relationship can be declined."
            );
        }

        relationship.setStatus(CollectionRelationshipStatus.DECLINED);

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }

    @Override
    public CollectionRelationshipResponseDTO revokeRelationship(UUID relationshipId, UUID profileId) {
        CollectionRelationship relationship = getEntityById(relationshipId);

        if (relationship.getStatus() != CollectionRelationshipStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only an active relationship can be revoked."
            );
        }

        CollectionRelationshipRole role = relationship.getRole();

        if (role == CollectionRelationshipRole.OWNER) {
            CollectionRelationshipResponseDTO revokedOwner = revokeOwnerRelationship(
                    relationship,
                    profileId
            );

            return revokedOwner;
        }

        if (!isActiveOwner(relationship.getCollectionId(), profileId)) {
            throw new IllegalStateException(
                    "Only an owner can revoke another member's relationship."
            );
        }

        relationship.setStatus(CollectionRelationshipStatus.REVOKED);

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }

    /*
     * The last active owner cannot revoke themselves or be downgraded.
     *
     * An owner also cannot revoke another owner's ownership.
     * An owner may voluntarily give up ownership only when another
     * active owner remains on the collection.
     */
    private CollectionRelationshipResponseDTO revokeOwnerRelationship(CollectionRelationship relationship, UUID profileId) {
        if (!relationship.getProfileId().equals(profileId)) {
            throw new IllegalStateException(
                    "An owner cannot revoke another owner's ownership."
            );
        }

        List<CollectionRelationship> activeOwners =
                collectionRelationshipRepository.findActiveOwnersByCollectionId(
                        relationship.getCollectionId()
                );

        if (activeOwners.size() <= 1) {
            throw new IllegalStateException(
                    "The last active owner cannot revoke themselves. Delete the collection instead."
            );
        }

        relationship.setRole(CollectionRelationshipRole.VIEW_ONLY);
        relationship.setStatus(CollectionRelationshipStatus.ACTIVE);

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }
      /*
      * WARNING:
      * This method intentionally bypasses the normal relationship status
      * business rules enforced by acceptRelationship(), declineRelationship(),
      * and revokeRelationship().
      *
      * Do NOT use this method in production application flows.
      * It is intended only for administrative, testing, migration, or other
      * controlled internal operations.
      */
    @Override
    public CollectionRelationshipResponseDTO updateRelationshipStatus(UUID relationshipId, CollectionRelationshipStatus status) {
    CollectionRelationship relationship = getEntityById(relationshipId);

    if (status == null) {
        throw new IllegalArgumentException(
                  "Relationship status cannot be null."
          );
      }

      relationship.setStatus(status);

      return mapToResponse(
              collectionRelationshipRepository.save(relationship)
      );
    }


    @Override
    public CollectionRelationshipResponseDTO updateRelationshipRole(UUID relationshipId, UUID profileId, CollectionRelationshipRole role) {
        CollectionRelationship relationship = getEntityById(relationshipId);

        if (!isActiveOwner(relationship.getCollectionId(), profileId)) {
            throw new IllegalStateException(
                    "Only an owner can change collection relationship roles."
            );
        }

        if (relationship.getRole() == CollectionRelationshipRole.OWNER &&
                role != CollectionRelationshipRole.OWNER) {

            List<CollectionRelationship> activeOwners =
                    collectionRelationshipRepository.findActiveOwnersByCollectionId(
                            relationship.getCollectionId()
                    );

            if (activeOwners.size() <= 1) {
                throw new IllegalStateException(
                        "The last active owner cannot be downgraded."
                );
            }
        }

        if (relationship.getRole() == CollectionRelationshipRole.OWNER &&
                !relationship.getProfileId().equals(profileId)) {

            throw new IllegalStateException(
                    "An owner cannot downgrade another owner."
            );
        }

        relationship.setRole(role);

        return mapToResponse(collectionRelationshipRepository.save(relationship));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionRelationshipResponseDTO> getProposedRelationshipsByCollectionIds(List<UUID> collectionIds) {
        return collectionRelationshipRepository
                .findProposedRelationshipsByCollectionIds(collectionIds)
                .stream()
                .map(this::mapProjectionToResponse)
                .toList();
    }

    

    private CollectionRelationship getEntityById(UUID relationshipId) {
        return collectionRelationshipRepository.findRelationshipById(relationshipId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collection relationship not found: " + relationshipId
                ));
    }

    private boolean isActiveOwner(UUID collectionId, UUID profileId) {
        return collectionRelationshipRepository
                .findActiveOwnerByCollectionIdAndProfileId(collectionId, profileId)
                .isPresent();
    }

    private CollectionRelationshipResponseDTO mapProjectionToResponse(ProposedRelationshipProjection projection) {
        return CollectionRelationshipResponseDTO.builder()
                .profileId(projection.getProfileId())
                .role(CollectionRelationshipRole.valueOf(projection.getRole()))
                .build();
    }

    private CollectionRelationshipResponseDTO mapToResponse(CollectionRelationship relationship) {
        return CollectionRelationshipResponseDTO.builder()
                .collectionRelationshipId(relationship.getCollectionRelationshipId())
                .collectionId(relationship.getCollectionId())
                .profileId(relationship.getProfileId())
                .role(relationship.getRole())
                .status(relationship.getStatus())
                .invitedByProfileId(relationship.getInvitedByProfileId())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .build();
    }
}
