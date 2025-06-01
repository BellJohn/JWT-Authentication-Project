package com.bellj.authserver.service;

import com.bellj.authserver.config.interceptor.HeaderInterceptor;
import com.bellj.authserver.model.UpstreamProfileRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class ResourceServerService {

    private final String resourceServerURL;
    private final HeaderInterceptor headerInterceptor;

    public ResourceServerService(@Value("${resourceServerURL:http://localhost:8080}") String resourceServerURL, HeaderInterceptor headerInterceptor) {
        this.resourceServerURL = resourceServerURL;
        this.headerInterceptor = headerInterceptor;
    }

    public URI createUserProfile(UpstreamProfileRequest upstreamProfileRequest) {
        return new RestTemplateBuilder()
                .interceptors(headerInterceptor)
                .build()
                .postForLocation(resourceServerURL + "/profile", upstreamProfileRequest);
    }
}
