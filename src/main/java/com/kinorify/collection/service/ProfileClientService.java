package com.kinorify.collection.service;

import com.kinorify.collection.dto.client.ProfileUserClientDTO;

import java.util.UUID;

public interface ProfileClientService {

    ProfileUserClientDTO getUserByCognitoSub(String cognitoSub);

    UUID getProfileIdByCognitoSub(String cognitoSub);

    ProfileUserClientDTO getUserById(UUID userId);
}
