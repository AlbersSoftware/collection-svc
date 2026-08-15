package com.kinorify.collection.dto.request;

import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MergeRequestDecisionRequestDTO(

        @NotNull
        MergeRequestDecisionStatus status,

        @Size(max = 2000)
        String note

) {
}
