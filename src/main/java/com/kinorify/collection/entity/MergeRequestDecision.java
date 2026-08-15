package com.kinorify.collection.entity;

import com.kinorify.collection.entity.enums.MergeRequestDecisionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "merge_request_decision",
    schema = "collection",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_merge_request_decision_request",
            columnNames = {"merge_request_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeRequestDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = "merge_request_decision_id",
        nullable = false,
        updatable = false
    )
    private UUID mergeRequestDecisionId;

    @Column(name = "merge_request_id", nullable = false)
    private UUID mergeRequestId;

    @Column(name = "decider_profile_id", nullable = false)
    private UUID deciderProfileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MergeRequestDecisionStatus status;

    @Column(name = "note")
    private String note;

    @Column(name = "responded_at", nullable = false, updatable = false)
    private OffsetDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        if (respondedAt == null) {
            respondedAt = OffsetDateTime.now();
        }
    }
}
