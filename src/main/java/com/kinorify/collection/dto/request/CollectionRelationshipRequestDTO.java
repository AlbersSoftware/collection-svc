package com.kinorify.collection.dto.request;

import com.kinorify.collection.entity.enums.CollectionRelationshipRole;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CollectionRelationshipRequestDTO(

        @NotNull
        UUID profileId,

        @NotNull
        CollectionRelationshipRole role

) {
}
