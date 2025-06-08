package com.bellj.authserver.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bellj.authserver.service.TokenService;
import com.nimbusds.jose.JOSEException;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class AuthorisationHeaderInterceptorTest {

  private AuthorisationHeaderInterceptor authorisationHeaderInterceptor;
  private final byte[] emptyBody = new byte[0];
  @Mock private TokenService mockTokenService;
  @Mock private HttpRequest mockRequest;

  @Mock private ClientHttpRequestExecution mockClientHttpRequestExecution;

  @BeforeEach
  void setUp() {
    Mockito.reset(mockTokenService, mockRequest, mockClientHttpRequestExecution);
    authorisationHeaderInterceptor = new AuthorisationHeaderInterceptor(mockTokenService);
  }

  @Test
  void testInterceptSuccess() throws IOException, JOSEException {
    // Given
    HttpHeaders httpHeaders = new HttpHeaders();
    when(mockRequest.getHeaders()).thenReturn(httpHeaders);
    when(mockTokenService.generatePrivilegedToken()).thenReturn("Signed Privileged JWT");

    // When
    try (ClientHttpResponse response =
        authorisationHeaderInterceptor.intercept(
            mockRequest, emptyBody, mockClientHttpRequestExecution)) {
      // Then
      assertTrue(httpHeaders.containsKey("Authorization"));
      assertEquals(
          "Bearer Signed Privileged JWT",
          Objects.requireNonNull(httpHeaders.get("Authorization")).get(0));
    }
  }
}
