package com.kinorify.collection.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionMediaResponseDTO(

        UUID collectionMediaId,

        UUID collectionId,

        UUID mediaId,

        UUID addedByProfileId,

        OffsetDateTime addedAt,

        OffsetDateTime removedAt,

        UUID removedByProfileId

) {
}
