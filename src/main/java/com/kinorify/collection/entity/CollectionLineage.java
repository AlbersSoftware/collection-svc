package com.kinorify.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "collection_lineage",
    schema = "collection",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_collection_lineage_collection_ancestor",
            columnNames = {
                "collection_id",
                "ancestor_collection_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionLineage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = "collection_lineage_id",
        nullable = false,
        updatable = false
    )
    private UUID collectionLineageId;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "ancestor_collection_id", nullable = false)
    private UUID ancestorCollectionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
