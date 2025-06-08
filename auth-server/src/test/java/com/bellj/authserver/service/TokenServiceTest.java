package com.bellj.authserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  private TokenService tokenService;

  private PublicKey publicKey;

  @BeforeEach
  void setUp() throws NoSuchAlgorithmException {
    // Mock key pair for test consistency
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();
    publicKey = keyPair.getPublic();

    tokenService = new TokenService(keyPair);
  }

  @Test
  void testGenerateUserToken() throws JOSEException, ParseException {
    // Given
    Long userId = 123L;

    // When
    String token = tokenService.generateUserToken(userId);

    // Then
    assertNotNull(token, "Token should not be null");

    // Parse and verify claims
    SignedJWT signedJWT = SignedJWT.parse(token);
    assertEquals("123", signedJWT.getJWTClaimsSet().getSubject());
    assertEquals("USER", signedJWT.getJWTClaimsSet().getClaim("ROLE"));
    assertEquals("auth-server", signedJWT.getJWTClaimsSet().getIssuer());
  }

  @Test
  void testGeneratePrivilegedToken() throws JOSEException, ParseException {

    // Given
    // When
    String token = tokenService.generatePrivilegedToken();

    // Then
    assertNotNull(token, "Token should not be null");

    // Parse and verify claims
    SignedJWT signedJWT = SignedJWT.parse(token);
    assertEquals("-1", signedJWT.getJWTClaimsSet().getSubject());
    assertEquals("PRIVILEGED", signedJWT.getJWTClaimsSet().getClaim("ROLE"));
    assertEquals("auth-server", signedJWT.getJWTClaimsSet().getIssuer());
  }

  @Test
  void testGetPublicKey() {
    // Given
    // When
    // Then
    assertEquals(publicKey, tokenService.getPublicKey(), "Public key should match expected value");
  }
}
