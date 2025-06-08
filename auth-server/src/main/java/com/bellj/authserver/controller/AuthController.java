package com.bellj.authserver.controller;

import com.bellj.authserver.model.LoginRequest;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.service.TokenService;
import com.bellj.authserver.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.interfaces.RSAPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Entrypoint for all user authentication. Handles user registration and login. */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Handles login/authentication requests")
public class AuthController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
  private final TokenService tokenService;
  private final UserService userService;

  public AuthController(TokenService tokenService, UserService userService) {
    this.tokenService = tokenService;
    this.userService = userService;
  }

  /**
   * Handles the login attempts for users. Successful logins result in a user role access JWT being
   * granted.
   *
   * @param loginRequest Request containing the user's credentials
   * @return JWT granting access to this user's resources
   */
  @PostMapping("/login")
  @Operation(summary = "User Login", description = "Authenticates user and returns a JWT token")
  public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
    LOGGER.info("Login request received for user: {}", loginRequest.username());

    return userService
        .getValidUserId(loginRequest.username(), loginRequest.password())
        .map(this::generateJwtResponse)
        .orElseGet(
            () -> {
              LOGGER.warn("Login failed for user: {}", loginRequest.username());
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            });
  }

  private ResponseEntity<String> generateJwtResponse(Long userId) {
    try {
      LOGGER.info("Token generation successful for user ID: {}", userId);
      return ResponseEntity.ok(tokenService.generateUserToken(userId));
    } catch (JOSEException e) {
      LOGGER.error("Token generation failed: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token generation error");
    }
  }

  /**
   * Handles the registration of new users. This results in a new user entity for this service to
   * manage and a profile under the resource server.
   *
   * @param registerRequest Request containing the users credentials plus profile data.
   * @return Simple outcome message
   */
  @PostMapping("/register")
  @Operation(summary = "User Registration", description = "Registers a new user")
  public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
    LOGGER.info("Register invoked");

    if (userService.createUser(registerRequest)) {
      return ResponseEntity.status(HttpStatus.CREATED).body("User Registered OK");
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User Registration Failed");
  }

  /**
   * Handles JWKS requests. Note. JWKS will be different each time the system is booted.
   *
   * @return The JWKS
   */
  @GetMapping("/jwks")
  @Operation(
      summary = "Return the JWKS",
      description =
          "Fetches the JWKS containing the public key for verifying the signature of the JWT")
  public ResponseEntity<String> getJWKS() {
    LOGGER.info("JWKS invoked");

    RSAKey rsaKey =
        new RSAKey.Builder((RSAPublicKey) tokenService.getPublicKey())
            .keyID("auth-server-key-id")
            .build();

    // Convert JWKSet to properly formatted JSON string
    String jwksJson = rsaKey.toPublicJWK().toJSONString();

    return ResponseEntity.ok("{\"keys\": [" + jwksJson + "]}");
  }
}
