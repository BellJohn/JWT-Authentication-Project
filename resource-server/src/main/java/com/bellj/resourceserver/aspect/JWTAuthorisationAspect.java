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
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Aspect for verifying the JWT provided in the HTTP Request. */
@Aspect
@Component
public class JWTAuthorisationAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthorisationAspect.class);
  private final JWKSRetriever jwksRetriever;

  public JWTAuthorisationAspect(JWKSRetriever jwksRetriever) {
    this.jwksRetriever = jwksRetriever;
  }

  /**
   * Any method annotated with the @JWTAuthorisation annotation will be woven with this advice
   * method. This ensures that the correct access role is present in the JWT to access a given
   * method. </br> E.g. a method annotated with @JWTAuthorisation("USER") must have a user access
   * token.
   *
   * @param joinPoint The method which has the annotation on it.
   * @param jwtAuthorisation The authorisation role that should be present.
   * @return The logic behind the target as access is granted.
   * @throws Throwable When access to the target method is not granted.
   */
  @Around("@annotation(jwtAuthorisation)")
  public Object validateJwt(ProceedingJoinPoint joinPoint, JWTAuthorisation jwtAuthorisation)
      throws Throwable {
    LOGGER.info("JWT Authorisation annotation invoked");

    HttpServletRequest request = getCurrentRequest();
    String token = request.getHeader("Authorization");
    String requestUserId = getRequestUserId(request);

    if (!isValidToken(token, jwtAuthorisation.value(), requestUserId)) {
      throw new UnauthorizedException("Invalid JWT token");
    }

    return joinPoint.proceed();
  }

  private HttpServletRequest getCurrentRequest() {
    return ((ServletRequestAttributes)
            Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
        .getRequest();
  }

  private String getRequestUserId(HttpServletRequest request) {
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      String[] pathSegments = request.getRequestURI().split("/");
      return pathSegments.length > 0 ? pathSegments[pathSegments.length - 1] : "";
    }
    return "";
  }

  private boolean isValidToken(String header, String expectedRole, String userId) {
    if (header == null || !header.startsWith("Bearer ")) {
      LOGGER.error("Invalid Authorization header.");
      return false;
    }

    try {
      SignedJWT jwt = SignedJWT.parse(header.substring("Bearer ".length()));
      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      String claimedRole = claims.getStringClaim("ROLE");
      String claimedUserId = claims.getSubject();

      return !invalidSignature(jwt)
          && !hasExpired(jwt)
          && isValidRole(claimedRole, expectedRole)
          && isValidUser(expectedRole, userId, claimedUserId);
    } catch (ParseException e) {
      LOGGER.warn("Malformed JWT token.", e);
      return false;
    }
  }

  private boolean isValidRole(String claimedRole, String expectedRole) {
    if (!claimedRole.equals(expectedRole)) {
      LOGGER.warn("Invalid role types provided: [{}], not [{}]", claimedRole, expectedRole);
      return false;
    }
    return true;
  }

  private boolean isValidUser(String expectedRole, String userId, String claimedUserId) {
    if ("USER".equals(expectedRole)
        && (userId == null || userId.isBlank() || !userId.equals(claimedUserId))) {
      LOGGER.warn(
          "User ID mismatch or missing: attempted access [{}] with credentials [{}]",
          userId,
          claimedUserId);
      return false;
    }
    return true;
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
      throw new RuntimeException("Error verifying signature", e);
    }

    LOGGER.info("Signature was valid");
    return false;
  }

  private boolean hasExpired(SignedJWT signedJWT) throws ParseException {
    boolean expired = signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date());
    if (expired) LOGGER.info("JWT has expired");
    return expired;
  }
}
