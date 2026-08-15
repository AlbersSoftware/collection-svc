package com.kinorify.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionRequestDTO(

        @NotBlank
        @Size(max = 200)
        String name,

        java.util.UUID thumbnailMediaId

) {
}
