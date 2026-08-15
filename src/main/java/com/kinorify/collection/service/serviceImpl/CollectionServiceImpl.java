package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.request.CollectionRequestDTO;
import com.kinorify.collection.dto.response.CollectionResponseDTO;
import com.kinorify.collection.entity.Collection;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.repository.CollectionRepository;
import com.kinorify.collection.service.CollectionRelationshipService;
import com.kinorify.collection.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionRelationshipService collectionRelationshipService;

    @Override
    public CollectionResponseDTO createCollection(CollectionRequestDTO request, UUID profileId) {
        Collection collection = Collection.builder()
                .name(request.name())
                .thumbnailMediaId(request.thumbnailMediaId())
                .createdByProfileId(profileId)
                .build();

        Collection saved = collectionRepository.save(collection);

        collectionRelationshipService.createOwnerRelationship(
                saved.getCollectionId(),
                profileId
        );

        /*
         * Future:
         * - Create initial lineage record.
         * - Publish collection-created event.
         */

        return mapToResponse(saved);
    }


    @Override
    public CollectionResponseDTO updateCollection(UUID collectionId, CollectionRequestDTO request, UUID profileId) {
        Collection collection = collectionRepository
                .findCollectionById(collectionId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found: " + collectionId
                ));

        requireActiveOwner(collectionId, profileId);

        collection.setName(request.name());
        collection.setThumbnailMediaId(request.thumbnailMediaId());

        Collection updated = collectionRepository.save(collection);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionResponseDTO getCollectionById(UUID collectionId) {
        Collection collection = collectionRepository
                .findCollectionById(collectionId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found: " + collectionId
                ));

        return mapToResponse(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollectionByName(String name) {
        return collectionRepository
                .findByName(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollectionsCreatedByProfileId(UUID profileId) {
        return collectionRepository
                .findCollectionsByCreatedByProfileId(profileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollectionsByProfileId(UUID profileId) {
        return collectionRepository
                .findCollectionsByProfileId(profileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteCollection(UUID collectionId, UUID profileId) {
        Collection collection = collectionRepository
                .findCollectionById(collectionId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found: " + collectionId
                ));

        requireActiveOwner(collectionId, profileId);

        List<CollectionRelationshipResponseDTO> activeOwners =
                collectionRelationshipService.getActiveOwnersByCollectionId(collectionId);

        if (activeOwners.size() > 1) {
            System.out.println(
                    "Cannot delete collection " + collectionId +
                    ": more than one active owner exists."
            );

            throw new IllegalStateException(
                    "A collection cannot be deleted while multiple owners are present."
            );
        }

        System.out.println(
                "Deleting collection " + collectionId +
                " because the requesting profile is the sole active owner."
        );

        collectionRepository.delete(collection);
    }

    private void requireActiveOwner(UUID collectionId, UUID profileId) {
        collectionRelationshipService
                .getActiveOwnerByCollectionIdAndProfileId(collectionId, profileId);
    }

    private CollectionResponseDTO mapToResponse(Collection collection) {
        return new CollectionResponseDTO(
                collection.getCollectionId(),
                collection.getName(),
                collection.getThumbnailMediaId(),
                collection.getCreatedByProfileId(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
