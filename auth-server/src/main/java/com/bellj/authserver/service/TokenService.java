package com.bellj.authserver.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.*;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Local service responsible for generating access JWTs. */
@Service
public class TokenService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
  private static final long FIFTEEN_MINUTES_IN_MILLIS = 15 * 60 * 1000;
  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  /**
   * This constructor should only ever be called by the spring dependency injection. If this is
   * called multiple times, there will be multiple live keypairs but only the latest is remembered.
   * </br> <em>Note:</em> A production system should not work like this. You should generate your
   * keys separately and have the service load them on boot. The application should not manage the
   * lifecycle of the keys but does for simplicity with this example.
   */
  public TokenService(KeyPair keyPair) {
    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();
  }

  /**
   * Generates a new user role token. This can be used to access resources belonging to this user
   * only.
   *
   * @param userId The User ID of the user this token should be for.
   * @return a user token as a JWT string
   * @throws JOSEException Something went wrong with the signing
   */
  public String generateUserToken(Long userId) throws JOSEException {
    LOGGER.info("Generating new user token");
    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject(String.valueOf(userId))
            .issuer("auth-server")
            .expirationTime(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES_IN_MILLIS))
            .claim("ROLE", "USER")
            .build();

    SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("auth-server-key-id").build(),
            claimsSet);

    signedJWT.sign(new RSASSASigner(privateKey));
    return signedJWT.serialize();
  }

  /**
   * Generates a new privileged role token. This is used for server to server communication and
   * could be used to access any resource. </br> <em>Note:</em> This token will live for 15 minutes.
   * That's suitable for the demo but there is no means of refreshing it. A production system needs
   * to consider whether caching it for the performance benefit outweighs the security implications
   * of credential storage. If a system does opt to store the token rather than regenerating it
   * everytime, the token must be encrypted for storage.
   *
   * @return A privileged token as a JWT string
   * @throws JOSEException Something went wrong with the signing.
   */
  public String generatePrivilegedToken() throws JOSEException {
    LOGGER.info("Generating new privileged token");
    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject(String.valueOf(-1))
            .issuer("auth-server")
            .expirationTime(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES_IN_MILLIS))
            .claim("ROLE", "PRIVILEGED")
            .build();

    SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("auth-server-key-id").build(),
            claimsSet);

    signedJWT.sign(new RSASSASigner(privateKey));
    return signedJWT.serialize();
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }
}
