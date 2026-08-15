package com.kinorify.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "collection_media",
    schema = "collection"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = "collection_media_id",
        nullable = false,
        updatable = false
    )
    private UUID collectionMediaId;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "added_by_profile_id", nullable = false)
    private UUID addedByProfileId;

    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    @Column(name = "removed_at")
    private OffsetDateTime removedAt;

    @Column(name = "removed_by_profile_id")
    private UUID removedByProfileId;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = OffsetDateTime.now();
        }
    }
}
