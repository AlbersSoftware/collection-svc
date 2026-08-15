package com.kinorify.collection.rest;

import com.kinorify.collection.config.JwtService;
import com.kinorify.collection.dto.response.CollectionMediaResponseDTO;
import com.kinorify.collection.service.CollectionMediaService;
import com.kinorify.collection.service.ProfileClientService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collection-media")
@RequiredArgsConstructor
public class CollectionMediaController {

    private final CollectionMediaService collectionMediaService;
    private final ProfileClientService profileClientService;
    private final JwtService jwtService;

    /*
     * -------------------------------------------------------------------------
     * Add media to collection
     * -------------------------------------------------------------------------
     *
     * OWNER and CAN_ADD profiles may add media.
     *
     * Permission rules are enforced by CollectionMediaService.
     * -------------------------------------------------------------------------
     */

    @PostMapping("/collections/{collectionId}/media/{mediaId}")
    public ResponseEntity<CollectionMediaResponseDTO> addMediaToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionMediaResponseDTO created =
                collectionMediaService.addMediaToCollection(
                        collectionId,
                        mediaId,
                        profileId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /*
     * -------------------------------------------------------------------------
     * Remove media from collection
     * -------------------------------------------------------------------------
     *
     * OWNER:
     * - may remove any media from the collection
     *
     * CAN_ADD:
     * - may remove only media they personally added
     *
     * VIEW_ONLY:
     * - may not remove media
     *
     * The CollectionMedia row is soft-removed rather than deleted.
     * -------------------------------------------------------------------------
     */

    @DeleteMapping("/collections/{collectionId}/media/{mediaId}")
    public ResponseEntity<CollectionMediaResponseDTO> removeMediaFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID profileId =
                getCurrentProfileId(jwt);

        CollectionMediaResponseDTO removed =
                collectionMediaService.removeMediaFromCollection(
                        collectionId,
                        mediaId,
                        profileId
                );

        return ResponseEntity.ok(
                removed
        );
    }

    /*
     * -------------------------------------------------------------------------
     * CollectionMedia relationship lookup
     * -------------------------------------------------------------------------
     */

    @GetMapping("/{collectionMediaId}")
    public ResponseEntity<CollectionMediaResponseDTO> getCollectionMediaById(
            @PathVariable UUID collectionMediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        CollectionMediaResponseDTO collectionMedia =
                collectionMediaService.getCollectionMediaById(
                        collectionMediaId
                );

        return ResponseEntity.ok(
                collectionMedia
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Display active media in a collection
     * -------------------------------------------------------------------------
     *
     * This is the normal display operation for a collection.
     *
     * It returns only active CollectionMedia relationships.
     * -------------------------------------------------------------------------
     */

    @GetMapping("/collections/{collectionId}/media")
    public ResponseEntity<List<CollectionMediaResponseDTO>> displayMedia(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionMediaResponseDTO> media =
                collectionMediaService.displayMedia(
                        collectionId
                );

        return ResponseEntity.ok(
                media
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Collection media history
     * -------------------------------------------------------------------------
     *
     * Includes both active and previously removed media.
     * -------------------------------------------------------------------------
     */

    @GetMapping("/collections/{collectionId}/media/all")
    public ResponseEntity<List<CollectionMediaResponseDTO>>
    getAllMediaByCollectionId(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionMediaResponseDTO> media =
                collectionMediaService
                        .getAllMediaByCollectionId(
                                collectionId
                        );

        return ResponseEntity.ok(
                media
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Active collections containing media
     * -------------------------------------------------------------------------
     *
     * Useful for determining which collections currently
     * reference a particular media item.
     * -------------------------------------------------------------------------
     */

    @GetMapping("/media/{mediaId}/collections")
    public ResponseEntity<List<CollectionMediaResponseDTO>>
    getActiveCollectionsByMediaId(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionMediaResponseDTO> collections =
                collectionMediaService
                        .getActiveCollectionsByMediaId(
                                mediaId
                        );

        return ResponseEntity.ok(
                collections
        );
    }

    /*
     * -------------------------------------------------------------------------
     * All collection history for media
     * -------------------------------------------------------------------------
     *
     * Includes collections from which the media was previously
     * removed.
     * -------------------------------------------------------------------------
     */

    @GetMapping("/media/{mediaId}/collections/all")
    public ResponseEntity<List<CollectionMediaResponseDTO>>
    getAllCollectionsByMediaId(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        getCurrentProfileId(jwt);

        List<CollectionMediaResponseDTO> collections =
                collectionMediaService
                        .getAllCollectionsByMediaId(
                                mediaId
                        );

        return ResponseEntity.ok(
                collections
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Current profile resolution
     * -------------------------------------------------------------------------
     */

    private UUID getCurrentProfileId(Jwt jwt) {
        String cognitoSub =
                jwtService.getCognitoSub(jwt);

        return profileClientService
                .getProfileIdByCognitoSub(
                        cognitoSub
                );
    }
}
