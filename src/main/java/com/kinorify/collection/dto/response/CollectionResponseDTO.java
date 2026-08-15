package com.kinorify.collection.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionResponseDTO(

        UUID collectionId,

        String name,

        UUID thumbnailMediaId,

        UUID createdByProfileId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}
