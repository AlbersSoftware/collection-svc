package com.kinorify.collection.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionLineageResponseDTO(

        UUID collectionId,

        UUID ancestorCollectionId,

        OffsetDateTime createdAt

) {
}
