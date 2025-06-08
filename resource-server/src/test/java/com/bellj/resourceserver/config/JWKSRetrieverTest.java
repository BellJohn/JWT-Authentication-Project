package com.bellj.resourceserver.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JWKSRetrieverTest {

  @InjectMocks private JWKSRetriever jwksRetriever;

  @Mock private JWKSet mockJwkSet;

  @BeforeEach
  void setUp() {
    jwksRetriever = new JWKSRetriever();
    ReflectionTestUtils.setField(jwksRetriever, "jwksUrl", "http://localhost:8080");
  }

  @Test
  void testGetJWKSet_ValidCache() {
    // Given: A previously cached JWKS within the refresh window
    ReflectionTestUtils.setField(jwksRetriever, "jwkSet", mockJwkSet);
    ReflectionTestUtils.setField(jwksRetriever, "retrievalTime", LocalDateTime.now());

    // When: JWKS is requested within 15 minutes
    JWKSet result = jwksRetriever.getJWKSet();

    // Then: Cached JWKS should be returned
    assertEquals(mockJwkSet, result);
  }

  @Test
  void testGetJWKSet_RefreshRequired() throws IOException, ParseException {
    // Given: A JWKS that is older than 15 minutes
    ReflectionTestUtils.setField(jwksRetriever, "jwkSet", mockJwkSet);
    ReflectionTestUtils.setField(
        jwksRetriever, "retrievalTime", LocalDateTime.now().minusMinutes(16));

    try (MockedStatic<JWKSet> mockedStatic = Mockito.mockStatic(JWKSet.class)) {
      mockedStatic.when(() -> JWKSet.load(any(URL.class))).thenReturn(mockJwkSet);

      // When
      JWKSet result = jwksRetriever.getJWKSet();

      // Then: JWKS should be refreshed
      assertEquals(mockJwkSet, result);
    }
  }

  @Test
  void testFetchNewJWKSet_ExceptionHandling() {

    // Given: URL fetch throws an exception
    ReflectionTestUtils.setField(jwksRetriever, "jwkSet", mockJwkSet);
    ReflectionTestUtils.setField(
        jwksRetriever, "retrievalTime", LocalDateTime.now().minusMinutes(16));

    try (MockedStatic<JWKSet> mockedStatic = Mockito.mockStatic(JWKSet.class)) {
      mockedStatic
          .when(() -> JWKSet.load(any(URL.class)))
          .thenThrow(new IOException("Test Exception"));

      // When
      assertThrows(RuntimeException.class, jwksRetriever::getJWKSet);
    }
  }
}
