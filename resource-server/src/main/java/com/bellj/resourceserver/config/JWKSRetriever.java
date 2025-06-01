package com.bellj.resourceserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;

@Component
public class JWKSRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWKSRetriever.class);

    private JWKSet jwkSet = null;
    private LocalDateTime retrievalTime = LocalDateTime.MIN;
    private @Value("${jwksUrl:http://localhost/8080/auth/jwks}") String jwksUrl;

    public JWKSet getJWKSet() {
        if (jwkSet == null || retrievalTime.isBefore(LocalDateTime.now().minusMinutes(15))) {
            try {
                jwkSet = fetchNewJWKSet();
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return jwkSet;
    }

    private JWKSet fetchNewJWKSet() throws IOException, ParseException {
        LOGGER.info("Retrieving new JWKS Set");
        JWKSet fetchedJWKSet = JWKSet.load(new URL(jwksUrl));
        if (fetchedJWKSet == null || fetchedJWKSet.isEmpty()) {
            LOGGER.error("Empty JWKS response");
            return null;
        }
        retrievalTime = LocalDateTime.now();

        return fetchedJWKSet;
    }
}
