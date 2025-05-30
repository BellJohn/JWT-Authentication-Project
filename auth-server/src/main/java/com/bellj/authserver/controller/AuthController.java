package com.bellj.authserver.controller;

import com.bellj.authserver.entity.User;
import com.bellj.authserver.model.LoginRequest;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.service.TokenService;
import com.bellj.authserver.service.UserService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Handles login/authentication requests")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final TokenService tokenService;
    private final UserService userService;

    public AuthController(TokenService tokenService, UserService userService){
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user and returns a JWT token")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        LOGGER.info("Login invoked with {}", loginRequest);

        Optional<Long> userId = userService.getValidUserId(loginRequest.username(), loginRequest.password());
        LOGGER.info("User ID retrieved: {}", userId);

        return userId.map(id -> {
            LOGGER.info("Access Granted");
            return ResponseEntity.ok(tokenService.generateToken(id));
        }).orElseGet(() -> {
            LOGGER.info("Access Denied");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED).getDetail());
        });
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Registers a new user")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest){
        LOGGER.info("Register invoked with {}", registerRequest);

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt(12)));
        if(userService.saveUser(user)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User Registered OK");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User Registration Failed");
    }

    @GetMapping("/jwks")
    @Operation(summary = "Return the JWKS", description = "Fetches the JWKS containing the public key for verifying the signature of the JWT")
    public ResponseEntity<JWKSet> getJWKS() throws ParseException {
        LOGGER.info("JWKS invoked");

        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) tokenService.getPublicKey())
                .keyID("my-key-id")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(new JWKSet(JWK.parse(rsaKey.toJSONObject())));
    }
}
