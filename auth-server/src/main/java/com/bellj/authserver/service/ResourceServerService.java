package com.bellj.authserver.service;

import com.bellj.authserver.interceptor.AuthorisationHeaderInterceptor;
import com.bellj.authserver.model.UpstreamProfileRequest;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

/** Service class responsible for managing calls to the Resource server. */
@Service
public class ResourceServerService {

  private final String resourceServerURL;
  private final AuthorisationHeaderInterceptor authorisationHeaderInterceptor;
  private final RestTemplateBuilder restTemplateBuilder;

  public ResourceServerService(
      @Value("${resourceServerURL:http://localhost:8080}") String resourceServerURL,
      AuthorisationHeaderInterceptor authorisationHeaderInterceptor,
      RestTemplateBuilder restTemplateBuilder) {
    this.resourceServerURL = resourceServerURL;
    this.authorisationHeaderInterceptor = authorisationHeaderInterceptor;
    this.restTemplateBuilder = restTemplateBuilder;
  }

  /**
   * Requests the Resource Service create a new Profile for the user.
   *
   * @param upstreamProfileRequest Request object to send to the Resource Server
   * @return The URI for the new entity stored in the Resource Server.
   */
  public URI createUserProfile(UpstreamProfileRequest upstreamProfileRequest) {
    return restTemplateBuilder
        .interceptors(authorisationHeaderInterceptor)
        .build()
        .postForLocation(resourceServerURL + "/profile", upstreamProfileRequest);
  }
}
