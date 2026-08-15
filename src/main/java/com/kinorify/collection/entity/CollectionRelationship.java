package com.kinorify.collection.entity;

import com.kinorify.collection.entity.enums.CollectionRelationshipRole;
import com.kinorify.collection.entity.enums.CollectionRelationshipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "collection_relationships",
    schema = "collection",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_collection_relationship_collection_profile",
            columnNames = {"collection_id", "profile_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collection_relationship_id", nullable = false, updatable = false)
    private UUID collectionRelationshipId;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private CollectionRelationshipRole role =
            CollectionRelationshipRole.VIEW_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CollectionRelationshipStatus status = CollectionRelationshipStatus.PENDING;

    @Column(name = "invited_by_profile_id")
    private UUID invitedByProfileId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
