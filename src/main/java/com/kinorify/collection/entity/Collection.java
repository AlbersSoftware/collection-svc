package com.kinorify.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "collections",
    schema = "collection"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collection_id", nullable = false, updatable = false)
    private UUID collectionId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "thumbnail_media_id")
    private UUID thumbnailMediaId;

    @Column(name = "created_by_profile_id", nullable = false, updatable = false)
    private UUID createdByProfileId;

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
