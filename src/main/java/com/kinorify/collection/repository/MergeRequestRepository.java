package com.kinorify.collection.repository;

import com.kinorify.collection.entity.MergeRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MergeRequestRepository
        extends JpaRepository<MergeRequest, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE merge_request_id = :mergeRequestId
        """, nativeQuery = true)
    Optional<MergeRequest> findMergeRequestById(
            @Param("mergeRequestId") UUID mergeRequestId
    );

    /*
     * Locks the merge request row for the duration of the
     * surrounding transaction.
     *
     * This is the concurrency mechanism used when an owner
     * commits a decision.
     */
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT m
    FROM MergeRequest m
    WHERE m.mergeRequestId = :mergeRequestId
    """)
Optional<MergeRequest> findMergeRequestByIdForUpdate(
        @Param("mergeRequestId") UUID mergeRequestId
);



    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE requested_by_profile_id = :profileId
        ORDER BY requested_at DESC
        """, nativeQuery = true)
    List<MergeRequest> findMergeRequestsByRequestedByProfileId(
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE status = 'PENDING'
          AND (
              source_collection_id = :collectionId
              OR target_collection_id = :collectionId
          )
        ORDER BY requested_at DESC
        """, nativeQuery = true)
    List<MergeRequest> findPendingMergeRequestsByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM collection.merge_request
            WHERE status = 'PENDING'
              AND (
                  source_collection_id = :collectionId
                  OR target_collection_id = :collectionId
              )
        )
        """, nativeQuery = true)
    boolean existsPendingMergeRequestByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE source_collection_id = :sourceCollectionId
          AND target_collection_id = :targetCollectionId
          AND status = 'PENDING'
        """, nativeQuery = true)
    Optional<MergeRequest> findPendingMergeRequest(
            @Param("sourceCollectionId") UUID sourceCollectionId,
            @Param("targetCollectionId") UUID targetCollectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE status = 'PENDING'
          AND (
              source_collection_id = :collectionId
              OR target_collection_id = :collectionId
          )
        """, nativeQuery = true)
    List<MergeRequest> findPendingMergeRequests(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE status = :status
        ORDER BY requested_at DESC
        """, nativeQuery = true)
    List<MergeRequest> findMergeRequestsByStatus(
            @Param("status") String status
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE target_collection_id = :collectionId
        ORDER BY requested_at DESC
        """, nativeQuery = true)
    List<MergeRequest> findAllMergeRequestByTargetCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.merge_request
        WHERE source_collection_id = :collectionId
        ORDER BY requested_at DESC
        """, nativeQuery = true)
    List<MergeRequest> findAllMergeRequestBySourceCollectionId(
            @Param("collectionId") UUID collectionId
    );
}
