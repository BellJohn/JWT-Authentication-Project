package com.bellj.authserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.bellj.authserver.interceptor.AuthorisationHeaderInterceptor;
import com.bellj.authserver.model.UpstreamProfileRequest;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ResourceServerServiceTest {

  private static final String RESOURCE_SERVER_URL = "http://localhost:8080";
  @Mock private RestTemplateBuilder restTemplateBuilder;
  @Mock private RestTemplate restTemplate;
  @Mock private AuthorisationHeaderInterceptor authorisationHeaderInterceptor;
  @InjectMocks private ResourceServerService resourceServerService;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilder.interceptors(authorisationHeaderInterceptor))
        .thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
  }

  @Test
  void testCreateUserProfileSuccess() {
    // Given
    UpstreamProfileRequest request = new UpstreamProfileRequest();
    URI expectedUri = URI.create(RESOURCE_SERVER_URL + "/profile/123");

    when(restTemplate.postForLocation(anyString(), any(UpstreamProfileRequest.class)))
        .thenReturn(expectedUri);
    ReflectionTestUtils.setField(
        resourceServerService, "resourceServerURL", "http://localhost:8080");

    // When
    URI resultUri = resourceServerService.createUserProfile(request);

    // Then
    assertNotNull(resultUri);
    assertEquals(expectedUri, resultUri);
    verify(restTemplate).postForLocation(eq(RESOURCE_SERVER_URL + "/profile"), eq(request));
  }
}
