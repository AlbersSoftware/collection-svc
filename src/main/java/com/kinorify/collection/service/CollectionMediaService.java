package com.kinorify.collection.service;

import com.kinorify.collection.dto.response.CollectionMediaResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CollectionMediaService {

    /**
     * Adds media to a collection.
     *
     * The profile must have permission to add media to the
     * collection.
     */
    CollectionMediaResponseDTO addMediaToCollection(
            UUID collectionId,
            UUID mediaId,
            UUID profileId
    );

    /**
     * Soft-removes media from a collection.
     *
     * The CollectionMedia record is retained for history and
     * removed_at / removed_by_profile_id are populated.
     */
    CollectionMediaResponseDTO removeMediaFromCollection(
            UUID collectionId,
            UUID mediaId,
            UUID profileId
    );

    /**
     * Gets a CollectionMedia relationship by its id.
     */
    CollectionMediaResponseDTO getCollectionMediaById(
            UUID collectionMediaId
    );

    /**
     * Returns the currently active media belonging to a collection.
     *
     * This is the method that can be used by normal collection
     * screens as well as merge proposal screens.
     */
    List<CollectionMediaResponseDTO> displayMedia(
            UUID collectionId
    );

    /**
     * Returns all CollectionMedia history for a collection,
     * including previously removed media.
     */
    List<CollectionMediaResponseDTO> getAllMediaByCollectionId(
            UUID collectionId
    );

    /**
     * Returns all active collection relationships for a media id.
     */
    List<CollectionMediaResponseDTO> getActiveCollectionsByMediaId(
            UUID mediaId
    );

    /**
     * Returns all collection history for a media id,
     * including removed relationships.
     */
    List<CollectionMediaResponseDTO> getAllCollectionsByMediaId(
            UUID mediaId
    );

    /**
     * Combines the active media from two collections for a merge.
     *
     * Duplicate media ids are removed.
     *
     * This method does not create any CollectionMedia records.
     */
    List<UUID> combineMediaForMerge(
            UUID sourceCollectionId,
            UUID targetCollectionId
    );

    /**
     * Adds the combined active media from the source and target
     * collections to the newly-created merged collection.
     *
     * Duplicate media ids are only added once.
     */
    List<CollectionMediaResponseDTO> addMediaToMergedCollection(
            UUID newCollectionId,
            UUID sourceCollectionId,
            UUID targetCollectionId,
            UUID addedByProfileId
    );
}
