package com.kinorify.collection.dto;

import com.kinorify.collection.entity.enums.CollectionRelationshipRole;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProposedRelationshipDTO(

        @NotNull
        UUID profileId,

        @NotNull
        CollectionRelationshipRole role

) {
}
