package com.kinorify.collection.dto;


import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MergeRequestDTO(

        @NotNull
        UUID targetCollectionId

) {
}
