package com.kinorify.collection.repository;

import com.kinorify.collection.entity.MergeRequestDecision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MergeRequestDecisionRepository
        extends JpaRepository<MergeRequestDecision, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.merge_request_decision
        WHERE merge_request_decision_id = :decisionId
        """, nativeQuery = true)
    Optional<MergeRequestDecision> findDecisionById(
            @Param("decisionId") UUID decisionId
    );

    /*
     * Locks the decision row while the decision is being
     * committed.
     */
    @Query(value = """
        SELECT *
        FROM collection.merge_request_decision
        WHERE merge_request_decision_id = :decisionId
        FOR UPDATE
        """, nativeQuery = true)
    Optional<MergeRequestDecision> findDecisionByIdForUpdate(
            @Param("decisionId") UUID decisionId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request_decision
        WHERE merge_request_id = :mergeRequestId
        """, nativeQuery = true)
    List<MergeRequestDecision> findDecisionByMergeRequestId(
            @Param("mergeRequestId") UUID mergeRequestId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request_decision
        WHERE decider_profile_id = :profileId
        ORDER BY responded_at DESC
        """, nativeQuery = true)
    List<MergeRequestDecision> findByDeciderProfileId(
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT status
        FROM collection.merge_request_decision
        WHERE merge_request_decision_id = :decisionId
        """, nativeQuery = true)
    Optional<String> findStatusByMergeRequestDecisionId(
            @Param("decisionId") UUID decisionId
    );

    /*
     * Mark the winning decision.
     */
    @Modifying
    @Query(value = """
        UPDATE collection.merge_request_decision
        SET status = :status,
            responded_at = CURRENT_TIMESTAMP
        WHERE merge_request_decision_id = :decisionId
          AND status = 'PENDING'
        """, nativeQuery = true)
    int commitDecision(
            @Param("decisionId") UUID decisionId,
            @Param("status") String status
    );

    /*
     * Once one owner commits, every other pending decision
     * for the same merge request is cancelled.
     *
     * The winning decision is explicitly excluded.
     */
    @Modifying
    @Query(value = """
        UPDATE collection.merge_request_decision
        SET status = 'CANCELLED',
            responded_at = CURRENT_TIMESTAMP
        WHERE merge_request_id = :mergeRequestId
          AND status = 'PENDING'
          AND merge_request_decision_id <> :winningDecisionId
        """, nativeQuery = true)
    int cancelPendingDecisions(
            @Param("mergeRequestId") UUID mergeRequestId,
            @Param("winningDecisionId") UUID winningDecisionId
    );
}
