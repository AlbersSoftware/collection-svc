package com.kinorify.collection.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String getCognitoSub(Jwt jwt) {
        return jwt.getSubject();
    }

    public String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    public Boolean getEmailVerified(Jwt jwt) {
        return jwt.getClaim("email_verified");
    }
}
