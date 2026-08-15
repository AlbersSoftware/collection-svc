package com.kinorify.collection.repository;

import com.kinorify.collection.entity.CollectionMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionMediaRepository
        extends JpaRepository<CollectionMedia, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE collection_media_id = :collectionMediaId
        """, nativeQuery = true)
    Optional<CollectionMedia> findCollectionMediaById(
            @Param("collectionMediaId") UUID collectionMediaId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE collection_id = :collectionId
          AND removed_at IS NULL
        ORDER BY added_at
        """, nativeQuery = true)
    List<CollectionMedia> findActiveMediaByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE collection_id = :collectionId
        ORDER BY added_at
        """, nativeQuery = true)
    List<CollectionMedia> findAllMediaByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE media_id = :mediaId
          AND removed_at IS NULL
        ORDER BY added_at
        """, nativeQuery = true)
    List<CollectionMedia> findActiveCollectionsByMediaId(
            @Param("mediaId") UUID mediaId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE media_id = :mediaId
        ORDER BY added_at
        """, nativeQuery = true)
    List<CollectionMedia> findAllCollectionsByMediaId(
            @Param("mediaId") UUID mediaId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_media
        WHERE collection_id = :collectionId
          AND media_id = :mediaId
          AND removed_at IS NULL
        """, nativeQuery = true)
    Optional<CollectionMedia> findActiveMediaByCollectionIdAndMediaId(
            @Param("collectionId") UUID collectionId,
            @Param("mediaId") UUID mediaId
    );
}
