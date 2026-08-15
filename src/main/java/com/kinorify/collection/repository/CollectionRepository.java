package com.kinorify.collection.repository;

import com.kinorify.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.collections
        WHERE collection_id = :collectionId
        """, nativeQuery = true)
    Optional<Collection> findCollectionById(
            @Param("collectionId") UUID collectionId
    );

    /*
     * Returns collections that were originally created by the profile.
     * This is based on collections.created_by_profile_id and does not
     * represent the profile's current relationship to the collection.
     */
    @Query(value = """
        SELECT *
        FROM collection.collections
        WHERE created_by_profile_id = :profileId
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Collection> findCollectionsByCreatedByProfileId(
            @Param("profileId") UUID profileId
    );

    /*
     * Returns all collections where the profile currently has an
     * ACTIVE relationship, regardless of whether the profile created
     * the collection or was subsequently invited/shared into it.
     */
    @Query(value = """
        SELECT c.*
        FROM collection.collections c
        INNER JOIN collection.collection_relationships cr
            ON cr.collection_id = c.collection_id
        WHERE cr.profile_id = :profileId
          AND cr.status = 'ACTIVE'
        ORDER BY c.created_at DESC
        """, nativeQuery = true)
    List<Collection> findCollectionsByProfileId(
            @Param("profileId") UUID profileId
    );

    @Query(value = """
        SELECT *
        FROM collection.collections
        WHERE name = :name
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Collection> findByName(
            @Param("name") String name
    );
}
