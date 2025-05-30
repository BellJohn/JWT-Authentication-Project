package com.bellj.resourceserver.controller;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@Controller
@RequestMapping("/profile")
@Tag(name = "Profile", description = "Handles profile requests")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @GetMapping("/{userId}")
    @Operation(summary = "Fetch User Profile", description = "Fetches a user profile. Request a valid Auth Token")
    public ResponseEntity<String> getProfile(@Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable String userId) throws ParseException {
        LOGGER.info("Profile invoked");
        LOGGER.info("Bearer token: [{}]", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing Valid Auth Header");
        }
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        JWT jwt = JWTParser.parse(token);
        String userIdClaim = jwt.getJWTClaimsSet().getSubject();
        LOGGER.info("Parsed JWT {}", jwt.getJWTClaimsSet().getClaims());
        LOGGER.info("Found userId claim {}", userIdClaim);
        if (userId.equals(userIdClaim)) {
            return ResponseEntity.ok().body("Valid Auth Header");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Provided JWT does not grant access to this resource");
        }
    }
}
