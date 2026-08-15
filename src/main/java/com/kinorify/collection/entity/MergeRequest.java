package com.kinorify.collection.entity;

import com.kinorify.collection.entity.enums.MergeRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "merge_request",
        schema = "collection"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "merge_request_id",
            nullable = false,
            updatable = false
    )
    private UUID mergeRequestId;

    @Column(name = "source_collection_id", nullable = false)
    private UUID sourceCollectionId;

    @Column(name = "target_collection_id", nullable = false)
    private UUID targetCollectionId;

    @Column(
            name = "requested_by_profile_id",
            nullable = false,
            updatable = false
    )
    private UUID requestedByProfileId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "proposed_relationships",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private List<ProposedRelationship> proposedRelationships;

    /*
     * These fields may be null while the merge request is
     * still being prepared.
     *
     * sendFinalProposal() is responsible for requiring the
     * proposed name before the proposal can be finalized.
     */
    @Column(
            name = "proposed_name",
            length = 255
    )
    private String proposedName;

    @Column(name = "proposed_thumbnail_media_id")
    private UUID proposedThumbnailMediaId;

    @Column(name = "new_collection_id")
    private UUID newCollectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MergeRequestStatus status =
            MergeRequestStatus.PENDING;

    @Column(
            name = "requested_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime requestedAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = OffsetDateTime.now();
        }
    }
}
