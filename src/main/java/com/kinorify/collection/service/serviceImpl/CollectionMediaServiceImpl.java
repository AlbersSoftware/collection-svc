package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.response.CollectionMediaResponseDTO;
import com.kinorify.collection.dto.response.CollectionRelationshipResponseDTO;
import com.kinorify.collection.entity.CollectionMedia;
import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.entity.enums.CollectionRelationshipStatus;
import com.kinorify.collection.repository.CollectionMediaRepository;
import com.kinorify.collection.service.CollectionMediaService;
import com.kinorify.collection.service.CollectionRelationshipService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionMediaServiceImpl implements CollectionMediaService {

    private final CollectionMediaRepository collectionMediaRepository;

    private final CollectionRelationshipService
            collectionRelationshipService;

    /*
     * -------------------------------------------------------------------------
     * Add media
     * -------------------------------------------------------------------------
     */

    @Override
    public CollectionMediaResponseDTO addMediaToCollection(
            UUID collectionId,
            UUID mediaId,
            UUID profileId
    ) {
        requireCanAddMedia(
                collectionId,
                profileId
        );

        /*
         * Prevent the same active media from being added to the
         * same collection more than once.
         */
        if (collectionMediaRepository
                .findActiveMediaByCollectionIdAndMediaId(
                        collectionId,
                        mediaId
                )
                .isPresent()) {

            throw new IllegalStateException(
                    "Media is already active in this collection"
            );
        }

        CollectionMedia collectionMedia =
                CollectionMedia.builder()
                        .collectionId(collectionId)
                        .mediaId(mediaId)
                        .addedByProfileId(profileId)
                        .build();

        return mapToResponse(
                collectionMediaRepository.save(
                        collectionMedia
                )
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Remove media
     * -------------------------------------------------------------------------
     */

@Override
public CollectionMediaResponseDTO removeMediaFromCollection(
        UUID collectionId,
        UUID mediaId,
        UUID profileId
) {
    CollectionMedia collectionMedia =
            collectionMediaRepository
                    .findActiveMediaByCollectionIdAndMediaId(
                            collectionId,
                            mediaId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Active media relationship not found for collection "
                                            + collectionId
                                            + " and media "
                                            + mediaId
                            )
                    );

    CollectionRelationshipResponseDTO relationship =
            collectionRelationshipService
                    .getRelationshipByCollectionIdAndProfileId(
                            collectionId,
                            profileId
                    );

    if (relationship.status()
            != CollectionRelationshipStatus.ACTIVE) {

        throw new IllegalStateException(
                "Profile does not have an active relationship with this collection"
        );
    }

    /*
     * Owners may remove any media from a collection they own.
     */
    if (relationship.role()
            == CollectionRelationshipRole.OWNER) {

        return removeMedia(
                collectionMedia,
                profileId
        );
    }

    /*
     * CAN_ADD users may remove media only when they were
     * the profile that originally added it.
     */
    if (relationship.role()
            == CollectionRelationshipRole.CAN_ADD) {

        if (!collectionMedia
                .getAddedByProfileId()
                .equals(profileId)) {

            throw new IllegalStateException(
                    "CAN_ADD users may only remove media they added"
            );
        }

        return removeMedia(
                collectionMedia,
                profileId
        );
    }

    throw new IllegalStateException(
            "Profile does not have permission to remove media from this collection"
    );
}

  

    /*
     * -------------------------------------------------------------------------
     * Reads
     * -------------------------------------------------------------------------
     */

    @Override
    @Transactional(readOnly = true)
    public CollectionMediaResponseDTO getCollectionMediaById(
            UUID collectionMediaId
    ) {
        return collectionMediaRepository
                .findCollectionMediaById(
                        collectionMediaId
                )
                .map(this::mapToResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Collection media relationship not found: "
                                        + collectionMediaId
                        )
                );
    }
//view one collection's current active media
    @Override
    @Transactional(readOnly = true)
    public List<CollectionMediaResponseDTO> displayMedia(
            UUID collectionId
    ) {
        /*
         * There is deliberately no concept of "proposed media".
         *
         * Merge proposal screens simply display the active media
         * currently belonging to the participating collections.
         */
        return collectionMediaRepository
                .findActiveMediaByCollectionId(
                        collectionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public List<CollectionMediaResponseDTO> getAllMediaByCollectionId(
            UUID collectionId
    ) {
        return collectionMediaRepository
                .findAllMediaByCollectionId(
                        collectionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionMediaResponseDTO> getActiveCollectionsByMediaId(
            UUID mediaId
    ) {
        return collectionMediaRepository
                .findActiveCollectionsByMediaId(
                        mediaId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionMediaResponseDTO> getAllCollectionsByMediaId(
            UUID mediaId
    ) {
        return collectionMediaRepository
                .findAllCollectionsByMediaId(
                        mediaId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * -------------------------------------------------------------------------
     * Merge media
     * -------------------------------------------------------------------------
     */
//preview combined media not persisted yet. this will be called on proposal screen before merges are final
    @Override
    @Transactional(readOnly = true)
    public List<UUID> combineMediaForMerge(
            UUID sourceCollectionId,
            UUID targetCollectionId
    ) {
        /*
         * A merge uses whatever media is currently ACTIVE in the
         * two participating collections.
         *
         * There is intentionally no separate proposed-media model.
         */
        List<CollectionMedia> sourceMedia =
                collectionMediaRepository
                        .findActiveMediaByCollectionId(
                                sourceCollectionId
                        );

        List<CollectionMedia> targetMedia =
                collectionMediaRepository
                        .findActiveMediaByCollectionId(
                                targetCollectionId
                        );

        /*
         * LinkedHashSet gives us:
         *
         * 1. duplicate removal
         * 2. predictable insertion ordering
         */
        Set<UUID> combinedMediaIds =
                new LinkedHashSet<>();

        sourceMedia
                .stream()
                .map(CollectionMedia::getMediaId)
                .forEach(combinedMediaIds::add);

        targetMedia
                .stream()
                .map(CollectionMedia::getMediaId)
                .forEach(combinedMediaIds::add);

        return List.copyOf(
                combinedMediaIds
        );
    }

    @Override
    public List<CollectionMediaResponseDTO> addMediaToMergedCollection(
            UUID newCollectionId,
            UUID sourceCollectionId,
            UUID targetCollectionId,
            UUID addedByProfileId
    ) {
        List<UUID> combinedMediaIds =
                combineMediaForMerge(
                        sourceCollectionId,
                        targetCollectionId
                );

        List<CollectionMediaResponseDTO> createdMedia =
                new ArrayList<>();

        for (UUID mediaId : combinedMediaIds) {

            /*
             * Defensive check in case this method is called on a
             * partially-populated merged collection.
             */
            if (collectionMediaRepository
                    .findActiveMediaByCollectionIdAndMediaId(
                            newCollectionId,
                            mediaId
                    )
                    .isPresent()) {

                continue;
            }

            /*
             * Merge media is already approved as part of the merge
             * operation. It is therefore added directly to the new
             * collection rather than going through a separate
             * invitation or approval process.
             */
            CollectionMedia collectionMedia =
                    CollectionMedia.builder()
                            .collectionId(
                                    newCollectionId
                            )
                            .mediaId(
                                    mediaId
                            )
                            .addedByProfileId(
                                    addedByProfileId
                            )
                            .build();

            CollectionMedia saved =
                    collectionMediaRepository.save(
                            collectionMedia
                    );

            createdMedia.add(
                    mapToResponse(saved)
            );
        }

        return createdMedia;
    }

    /*
     * -------------------------------------------------------------------------
     * Authorization
     * -------------------------------------------------------------------------
     */

    private void requireCanAddMedia(
            UUID collectionId,
            UUID profileId
    ) {
        CollectionRelationshipResponseDTO relationship =
                collectionRelationshipService
                        .getRelationshipByCollectionIdAndProfileId(
                                collectionId,
                                profileId
                        );

        if (relationship.status()
                != CollectionRelationshipStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Profile does not have an active relationship with this collection"
            );
        }

        if (relationship.role()
                != CollectionRelationshipRole.OWNER
                && relationship.role()
                != CollectionRelationshipRole.CAN_ADD) {

            throw new IllegalStateException(
                    "Profile does not have permission to add media to this collection"
            );
        }
    }

    private void requireActiveOwner(
            UUID collectionId,
            UUID profileId
    ) {
        collectionRelationshipService
                .getActiveOwnerByCollectionIdAndProfileId(
                        collectionId,
                        profileId
                );
    }




      /*
     * -------------------------------------------------------------------------
     * Internal media helpers
     * -------------------------------------------------------------------------
     */

    private CollectionMediaResponseDTO removeMedia(
            CollectionMedia collectionMedia,
            UUID profileId
    ) {
        collectionMedia.setRemovedAt(
                OffsetDateTime.now()
        );

        collectionMedia.setRemovedByProfileId(
                profileId
        );

        return mapToResponse(
                collectionMediaRepository.save(
                        collectionMedia
                )
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Response mapping
     * -------------------------------------------------------------------------
     */

    private CollectionMediaResponseDTO mapToResponse(
            CollectionMedia collectionMedia
    ) {
        return new CollectionMediaResponseDTO(
                collectionMedia.getCollectionMediaId(),
                collectionMedia.getCollectionId(),
                collectionMedia.getMediaId(),
                collectionMedia.getAddedByProfileId(),
                collectionMedia.getAddedAt(),
                collectionMedia.getRemovedAt(),
                collectionMedia.getRemovedByProfileId()
        );
    }
}
