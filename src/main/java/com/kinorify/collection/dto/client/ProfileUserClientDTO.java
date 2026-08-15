package com.kinorify.collection.dto.client;

import java.util.UUID;

public record ProfileUserClientDTO(
     UUID userId,

    String cognitoSub

) {
}
