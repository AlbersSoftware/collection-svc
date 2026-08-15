package com.kinorify.collection.repository;

import com.kinorify.collection.entity.CollectionLineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface CollectionLineageRepository
        extends JpaRepository<CollectionLineage, UUID> {

    @Query(value = """
        SELECT *
        FROM collection.collection_lineage
        WHERE collection_lineage_id = :lineageId
        """, nativeQuery = true)
    java.util.Optional<CollectionLineage> findLineageById(
            @Param("lineageId") UUID lineageId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_lineage
        WHERE collection_id = :collectionId
        ORDER BY created_at
        """, nativeQuery = true)
    List<CollectionLineage> findLineageByCollectionId(
            @Param("collectionId") UUID collectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_lineage
        WHERE ancestor_collection_id = :ancestorCollectionId
        ORDER BY created_at
        """, nativeQuery = true)
    List<CollectionLineage> findDescendantsByAncestorCollectionId(
            @Param("ancestorCollectionId") UUID ancestorCollectionId
    );

    @Query(value = """
        SELECT *
        FROM collection.collection_lineage
        WHERE collection_id = :collectionId
          AND ancestor_collection_id = :ancestorCollectionId
        """, nativeQuery = true)
    Optional<CollectionLineage> findLineageRelationship(
            @Param("collectionId") UUID collectionId,
            @Param("ancestorCollectionId") UUID ancestorCollectionId
    );
}
