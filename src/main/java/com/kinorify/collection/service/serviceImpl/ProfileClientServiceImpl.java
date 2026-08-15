package com.kinorify.collection.service.impl;

import com.kinorify.collection.dto.client.ProfileUserClientDTO;
import com.kinorify.collection.service.ProfileClientService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileClientServiceImpl
        implements ProfileClientService {

    private final WebClient profileWebClient;

    @Override
    public ProfileUserClientDTO getUserByCognitoSub(String cognitoSub) {
        return profileWebClient
                .get()
                .uri(
                        "/api/users/cognito/{cognitoSub}",
                        cognitoSub
                )
                .retrieve()
                .bodyToMono(
                        ProfileUserClientDTO.class
                )
                .block();
    }

    @Override
    public UUID getProfileIdByCognitoSub(String cognitoSub) {
        ProfileUserClientDTO user =
                getUserByCognitoSub(
                        cognitoSub
                );

        if (user == null || user.userId() == null) {
            throw new IllegalStateException(
                    "Unable to resolve profile ID for authenticated user"
            );
        }

        return user.userId();
    }

    @Override
    public ProfileUserClientDTO getUserById(UUID userId) {
        return profileWebClient
                .get()
                .uri(
                        "/api/users/{userId}",
                        userId
                )
                .retrieve()
                .bodyToMono(
                        ProfileUserClientDTO.class
                )
                .block();
    }
}
