package com.kinorify.collection.dto.response;

import com.kinorify.collection.dto.ProposedRelationshipDTO;
import com.kinorify.collection.entity.enums.MergeRequestStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MergeRequestResponseDTO(

        UUID mergeRequestId,

        UUID sourceCollectionId,

        UUID targetCollectionId,

        UUID requestedByProfileId,

        UUID newCollectionId,

        MergeRequestStatus status,

        List<ProposedRelationshipDTO> proposedRelationships,

        String proposedName,

        UUID proposedThumbnailMediaId,

        OffsetDateTime requestedAt,

        OffsetDateTime respondedAt

) {
}
