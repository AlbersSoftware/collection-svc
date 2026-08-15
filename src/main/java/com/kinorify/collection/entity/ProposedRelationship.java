package com.kinorify.collection.entity;

import com.kinorify.collection.entity.enums.CollectionRelationshipRole;

import java.util.UUID;

public record ProposedRelationship(
        UUID profileId,
        CollectionRelationshipRole role
) {
}
