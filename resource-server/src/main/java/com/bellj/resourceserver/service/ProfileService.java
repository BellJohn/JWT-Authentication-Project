package com.bellj.resourceserver.service;

import com.bellj.resourceserver.dto.ProfileDTO;
import com.bellj.resourceserver.entity.Profile;
import com.bellj.resourceserver.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Optional<Profile> getProfile(Long userId) {
        Optional<Profile> optionalProfile = profileRepository.findByUserId(userId);
        LOGGER.info("Profile retrieved: {}", optionalProfile);

        return optionalProfile;
    }

    public Profile saveProfile(ProfileDTO profileDTO) {

        Profile profile = new Profile();
        profile.setUserId(profileDTO.userId());
        profile.setFirstname(profileDTO.firstname());
        profile.setLastname(profileDTO.lastname());
        profile.setPhoneNumber(profileDTO.phoneNumber());

        return profileRepository.save(profile);
    }
}
