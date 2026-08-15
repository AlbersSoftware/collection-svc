package com.kinorify.collection.dto.client;

import java.util.UUID;

public record ProfileClientResponseDTO(
        UUID userId,
        String displayName,
        String email
) {
}
