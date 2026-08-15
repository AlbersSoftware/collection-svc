package com.kinorify.collection.dto.response;

import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.entity.enums.CollectionRelationshipStatus;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
@Builder
public record CollectionRelationshipResponseDTO(

        UUID collectionRelationshipId,

        UUID collectionId,

        UUID profileId,

        CollectionRelationshipRole role,

        CollectionRelationshipStatus status,

        UUID invitedByProfileId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}
