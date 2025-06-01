package com.bellj.authserver.config.interceptor;

import com.bellj.authserver.service.TokenService;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;

@Component
public class HeaderInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderInterceptor.class);
    private final TokenService tokenService;

    public HeaderInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {

        LOGGER.info("Interceptor called");
        HttpHeaders headers = request.getHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        LOGGER.info("Headers: {}", headers.toSingleValueMap());
        return execution.execute(request, body);
    }

    private String getToken() {
        try {
            return tokenService.generatePrivilegedToken();
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}