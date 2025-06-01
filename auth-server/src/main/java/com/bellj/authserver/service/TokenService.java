package com.bellj.authserver.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.*;
import java.text.ParseException;
import java.util.Date;

@Service
public class TokenService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    private static final long FIFTEEN_MINUTES_IN_MILLIS = 15 * 60 * 1000;

    public TokenService() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public String generateUserToken(Long userId) throws JOSEException, ParseException {
        LOGGER.info("Generating new user token");
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userId))
                .issuer("auth-server")
                .expirationTime(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES_IN_MILLIS))
                .claim("ROLE", "USER")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("my-key-id").build(), claimsSet);

        signedJWT.sign(new RSASSASigner(privateKey));
        return signedJWT.serialize();
    }

    public String generatePrivilegedToken() throws JOSEException, ParseException {
            LOGGER.info("Generating new privileged token");
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(-1))
                    .issuer("auth-server")
                    .expirationTime(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES_IN_MILLIS))
                    .claim("ROLE", "PRIVILEGED")
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("my-key-id").build(), claimsSet);

            signedJWT.sign(new RSASSASigner(privateKey));
        return signedJWT.serialize();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
