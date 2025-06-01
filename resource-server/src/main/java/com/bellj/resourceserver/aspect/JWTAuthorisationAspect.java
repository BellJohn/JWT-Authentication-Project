package com.bellj.resourceserver.aspect;

import com.bellj.resourceserver.config.JWKSRetriever;
import com.bellj.resourceserver.exception.UnauthorizedException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Aspect
@Component
public class JWTAuthorisationAspect {

    public static Logger LOGGER = LoggerFactory.getLogger(JWTAuthorisationAspect.class);
    public JWKSRetriever jwksRetriever;

    public JWTAuthorisationAspect(JWKSRetriever jwksRetriever) {
        this.jwksRetriever = jwksRetriever;
    }

    @Around(value = "@annotation(jwtAuthorisation)")
    public Object validateJwt(ProceedingJoinPoint joinPoint, JWTAuthorisation jwtAuthorisation) throws Throwable {
        LOGGER.info("JWT Authorisation annotation invoked");
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader("Authorization");

        // Extract `userId` from the request URI
        String requestUserId = "";
        if (request.getMethod().equalsIgnoreCase("GET")) {
            String requestURI = request.getRequestURI();
            String[] pathSegments = requestURI.split("/");
            requestUserId = pathSegments[pathSegments.length - 1]; // Assuming userId is the last segment
        }
        if (!isValidToken(token, jwtAuthorisation.value(), requestUserId)) {
            throw new UnauthorizedException("Invalid JWT token");
        }

        return joinPoint.proceed();
    }

    private boolean isValidToken(String header, String expectedRole, String userId) {
        if (header == null || !header.startsWith("Bearer ")) {
            LOGGER.error("Invalid Authorization header.");
            return false;
        }

        String authToken = header.substring("Bearer ".length());

        try {
            SignedJWT jwt = SignedJWT.parse(authToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String claimedRole = claims.getStringClaim("ROLE");
            String claimedUserID = claims.getSubject();
            if (invalidSignature(jwt) || hasExpired(jwt) || hasInvalidRoleType(claimedRole, expectedRole)) {
                return false;
            }

            if ("USER".equals(expectedRole)) {
                if (userId == null || userId.isBlank()) {
                    LOGGER.info("Missing userId");
                    return false;
                }
                if (!userId.equals(claimedUserID)) {
                    LOGGER.warn("User ID mismatch: attempted access [{}] with credentials of [{}]", userId, claimedUserID);
                    return false;
                }
            }
            return true;
        } catch (ParseException e) {
            LOGGER.warn("Malformed JWT token.", e);
            return false;
        }
    }

    private boolean hasInvalidRoleType(String claimedRole, String expectedRole) {
        if (!claimedRole.equals(expectedRole)) {
            LOGGER.warn("Invalid role types provided: [{}], not [{}]", claimedRole, expectedRole);
            return true;
        }
        return false;
    }

    private boolean invalidSignature(SignedJWT jwt) {
        try {
            RSAKey publicKey = jwksRetriever.getJWKSet().getKeys().get(0).toRSAKey();
            JWSVerifier verifier = new RSASSAVerifier(publicKey.toRSAPublicKey());
            if (!verifier.verify(jwt.getHeader(), jwt.getSigningInput(), jwt.getSignature())) {
                LOGGER.warn("Signature was invalid");
                return true;
            }
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Signature was valid");
        return false;
    }

    private boolean hasExpired(SignedJWT signedJWT) throws ParseException {
        if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date(System.currentTimeMillis()))) {
            LOGGER.info("JWT has expired");
            return true;
        }
        return false;
    }
}