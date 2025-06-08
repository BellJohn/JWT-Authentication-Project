package com.bellj.resourceserver.service;

import com.bellj.resourceserver.dto.ProfileDTO;
import com.bellj.resourceserver.entity.Profile;
import com.bellj.resourceserver.repository.ProfileRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Primary business logic class for this application. Responsible for orchestrating the Profile
 * creation and reads.
 */
@Service
public class ProfileService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

  private final ProfileRepository profileRepository;

  public ProfileService(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  /**
   * Fetches an optional profile from the database with the given user ID. If none is found the
   * optional contains null.
   *
   * @param userId The User ID to look for in the database
   * @return Optional Profile
   */
  public Optional<Profile> getProfile(Long userId) {
    Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);
    LOGGER.info("Profile retrieved: {}", optionalProfile);

    return optionalProfile;
  }

  /**
   * Takes a Profile DTO and maps it to a Profile entity for storing in the database. If storage is
   * successful the updated entity is returned.
   *
   * @param profileDTO The profile data to store
   * @return The entity once it's been saved in the database
   */
  public Profile saveProfile(ProfileDTO profileDTO) {

    Profile profile = new Profile();
    profile.setUserId(profileDTO.userId());
    profile.setFirstname(profileDTO.firstname());
    profile.setLastname(profileDTO.lastname());
    profile.setPhoneNumber(profileDTO.phoneNumber());

    return profileRepository.save(profile);
  }
}
