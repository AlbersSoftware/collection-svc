package com.kinorify.collection.repository;

import com.kinorify.collection.entity.CollectionRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kinorify.collection.repository.projection.ProposedRelationshipProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRelationshipRepository
        extends JpaRepository<CollectionRelationship, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_relationship_id = :relationshipId
        """, nativeQuery = true)
    Optional<CollectionRelationship> findRelationshipById(
            @Param("relationshipId") UUID relationshipId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
        ORDER BY created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findRelationshipsByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
          AND profile_id = :profileId
        """, nativeQuery = true)
    Optional<CollectionRelationship> findRelationshipByCollectionIdAndProfileId(
            @Param("collectionId") UUID collectionId,
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
          AND role = 'OWNER'
          AND status = 'ACTIVE'
        ORDER BY created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findActiveOwnersByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
          AND profile_id = :profileId
          AND role = 'OWNER'
          AND status = 'ACTIVE'
        """, nativeQuery = true)
    Optional<CollectionRelationship> findActiveOwnerByCollectionIdAndProfileId(
            @Param("collectionId") UUID collectionId,
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE profile_id = :profileId
          AND status = 'ACTIVE'
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<CollectionRelationship> findActiveRelationshipsByProfileId(
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id IN (:collectionIds)
          AND status = 'ACTIVE'
        ORDER BY collection_id, created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findActiveRelationshipsByCollectionIds(
            @Param("collectionIds") List<UUID> collectionIds
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE role = :role
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<CollectionRelationship> findRelationshipsByRole(
            @Param("role") String role
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE status = :status
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<CollectionRelationship> findRelationshipsByStatus(
            @Param("status") String status
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id IN (:collectionIds)
          AND status = 'PENDING'
        ORDER BY collection_id, created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findPendingRelationshipsByCollectionIds(
            @Param("collectionIds") List<UUID> collectionIds
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id IN (:collectionIds)
          AND status = 'DECLINED'
        ORDER BY collection_id, created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findDeclinedRelationshipsByCollectionIds(
            @Param("collectionIds") List<UUID> collectionIds
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id IN (:collectionIds)
          AND status = 'REVOKED'
        ORDER BY collection_id, created_at
        """, nativeQuery = true)
    List<CollectionRelationship> findRevokedRelationshipsByCollectionIds(
            @Param("collectionIds") List<UUID> collectionIds
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
          AND invited_by_profile_id = :profileId
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<CollectionRelationship> findRelationshipsInvitedByProfileIdByCollectionId(
            @Param("profileId") UUID profileId,
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT status
        FROM collection.collection_relationships
        WHERE collection_id = :collectionId
        ORDER BY status
        """, nativeQuery = true)
    List<String> findStatusesByCollectionId(
            @Param("collectionId") UUID collectionId
    );



    @Query(value = """
    WITH combined_relationships AS (
        SELECT
            profile_id,
            role
        FROM collection.collection_relationships
        WHERE collection_id IN (:collectionIds)
          AND status = 'ACTIVE'
    ),
    ranked_relationships AS (
        SELECT
            profile_id,
            role,
            ROW_NUMBER() OVER (
                PARTITION BY profile_id
                ORDER BY
                    CASE role
                        WHEN 'OWNER' THEN 3
                        WHEN 'CAN_ADD' THEN 2
                        WHEN 'VIEW_ONLY' THEN 1
                    END DESC
            ) AS role_rank
        FROM combined_relationships
    )
    SELECT
        profile_id AS profileId,
        role
    FROM ranked_relationships
    WHERE role_rank = 1
    ORDER BY
        CASE role
            WHEN 'OWNER' THEN 3
            WHEN 'CAN_ADD' THEN 2
            WHEN 'VIEW_ONLY' THEN 1
        END DESC,
        profile_id
    """, nativeQuery = true)
List<ProposedRelationshipProjection> findProposedRelationshipsByCollectionIds(
        @Param("collectionIds") List<UUID> collectionIds
);

    @Query(value = """
    SELECT *
    FROM collection.collection_relationships
    WHERE profile_id = :profileId
      AND status = 'PENDING'
    ORDER BY created_at DESC
    """, nativeQuery = true)
List<CollectionRelationship> findPendingRelationshipsByProfileId(
        @Param("profileId") UUID profileId
);

@Query(value = """
    SELECT *
    FROM collection.collection_relationships
    WHERE invited_by_profile_id = :profileId
    ORDER BY created_at DESC
    """, nativeQuery = true)
List<CollectionRelationship> findRelationshipsInvitedByProfileId(
        @Param("profileId") UUID profileId
);
    // findProposedRelationshipsByCollectionIds reserved for MergeRequestService
}
