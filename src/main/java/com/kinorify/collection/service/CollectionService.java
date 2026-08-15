package com.kinorify.collection.service;

import com.kinorify.collection.dto.request.CollectionRequestDTO;
import com.kinorify.collection.dto.response.CollectionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CollectionService {

    CollectionResponseDTO createCollection(CollectionRequestDTO request, UUID profileId);

    CollectionResponseDTO updateCollection(UUID collectionId, CollectionRequestDTO request, UUID profileId);

    CollectionResponseDTO getCollectionById(UUID collectionId);

    List<CollectionResponseDTO> getCollectionByName(String name);

    List<CollectionResponseDTO> getCollectionsCreatedByProfileId(UUID profileId);

    List<CollectionResponseDTO> getCollectionsByProfileId(UUID profileId);

    void deleteCollection(UUID collectionId, UUID profileId);
}
