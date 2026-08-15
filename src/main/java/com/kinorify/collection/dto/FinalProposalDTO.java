package com.kinorify.collection.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FinalProposalDTO(

        @NotBlank
        String proposedName,

        UUID proposedThumbnailMediaId,

        @NotNull
        @Valid
        List<ProposedRelationshipDTO> proposedRelationships

) {
}
