package com.bellj.authserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;
import java.util.Date;

@Service
public class TokenService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public TokenService() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public String generateToken(Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        return JWT.create()
                .withIssuer("auth-server")
                .withSubject(String.valueOf(userId))
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // 1-hour expiry
                .sign(algorithm);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
