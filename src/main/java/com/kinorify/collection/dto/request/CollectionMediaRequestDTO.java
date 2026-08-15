package com.kinorify.collection.dto.media;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CollectionMediaRequestDTO(

        @NotNull
        UUID mediaId

) {
}
