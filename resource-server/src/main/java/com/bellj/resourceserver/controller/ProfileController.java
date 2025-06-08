package com.bellj.resourceserver.controller;

import com.bellj.resourceserver.aspect.JWTAuthorisation;
import com.bellj.resourceserver.dto.ProfileDTO;
import com.bellj.resourceserver.entity.Profile;
import com.bellj.resourceserver.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for all interactions with this service. This covers the relevant CRUD
 * features for the user profiles.
 */
@Controller
@RequestMapping("/profile")
@Tag(name = "Profile", description = "Handles profile requests")
public class ProfileController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  /**
   * Retrieves a profile from storage if the user ID requested matches the one in the JWT and if it
   * exists.
   *
   * @param userId The User ID of the Profile to retrieve.
   * @return The user's Profile or a 404 if not found.
   */
  @GetMapping("/{userId}")
  @Operation(
      summary = "Fetch User Profile",
      description = "Fetches a user profile. Request a valid Auth Token")
  @JWTAuthorisation("USER")
  public ResponseEntity<Profile> getProfile(@PathVariable String userId) {
    LOGGER.info("Get User Profile Invoked");

    Optional<Profile> optionalProfile = profileService.getProfile(Long.valueOf(userId));

    return optionalProfile
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Creates a profile in storage if the JWT is privileged. Cannot be accessed with a User role
   * token.
   *
   * @param profileDTO The profile data to store
   * @return The ID of the record created.
   */
  @PostMapping
  @Operation(
      summary = "Create New User Profile",
      description = "Creates a new user profile. Providing the userId back")
  @JWTAuthorisation("PRIVILEGED")
  public ResponseEntity<Long> createProfile(@RequestBody ProfileDTO profileDTO) {
    LOGGER.info("Create User Profile Invoked");

    Profile savedProfile = profileService.saveProfile(profileDTO);

    return ResponseEntity.ok(savedProfile.getId());
  }
}
