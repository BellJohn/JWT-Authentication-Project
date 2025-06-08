package com.bellj.authserver.interceptor;

import com.bellj.authserver.service.TokenService;
import com.nimbusds.jose.JOSEException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Http Request Interceptor responsible for ensuring a privileged authorisation token is present in
 * outbound requests
 */
@Component
public class AuthorisationHeaderInterceptor implements ClientHttpRequestInterceptor {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AuthorisationHeaderInterceptor.class);
  private final TokenService tokenService;

  public AuthorisationHeaderInterceptor(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public ClientHttpResponse intercept(
      @NonNull HttpRequest request,
      @NonNull byte[] body,
      @NonNull ClientHttpRequestExecution execution)
      throws IOException {

    LOGGER.info("Interceptor called");
    HttpHeaders headers = request.getHeaders();
    headers.set("Authorization", "Bearer " + getToken());
    return execution.execute(request, body);
  }

  private String getToken() {
    try {
      return tokenService.generatePrivilegedToken();
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
  }
}
