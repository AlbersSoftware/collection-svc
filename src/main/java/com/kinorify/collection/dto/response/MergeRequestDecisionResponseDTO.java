package com.kinorify.collection.dto.response;

import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MergeRequestDecisionResponseDTO(

        UUID mergeRequestDecisionId,

        UUID mergeRequestId,

        UUID deciderProfileId,

        MergeRequestDecisionStatus status,

        String note,

        OffsetDateTime respondedAt

) {
}
