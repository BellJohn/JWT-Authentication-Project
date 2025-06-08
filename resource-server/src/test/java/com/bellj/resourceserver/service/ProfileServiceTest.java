package com.bellj.resourceserver.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bellj.resourceserver.dto.ProfileDTO;
import com.bellj.resourceserver.entity.Profile;
import com.bellj.resourceserver.repository.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock private ProfileRepository profileRepository;

  @InjectMocks private ProfileService profileService;

  @Test
  void testGetProfile_Found() {
    // Given: A profile exists for userId = 123
    Profile mockProfile = new Profile();
    mockProfile.setId(123L);
    when(profileRepository.findByUserId(123L)).thenReturn(Optional.of(mockProfile));

    // When: getProfile() is called
    Optional<Profile> result = profileService.getProfile(123L);

    // Then: The profile should be returned
    assertTrue(result.isPresent());
    assertEquals(mockProfile, result.get());
  }

  @Test
  void testGetProfile_NotFound() {
    // Given: No profile exists for userId = 999
    when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

    // When: getProfile() is called
    Optional<Profile> result = profileService.getProfile(999L);

    // Then: An empty result should be returned
    assertTrue(result.isEmpty());
  }

  @Test
  void testSaveProfile_Success() {
    // Given: A valid ProfileDTO and expected saved Profile
    ProfileDTO profileDTO = new ProfileDTO(1L, "John", "Doe", "123456789");
    Profile savedProfile = new Profile();
    savedProfile.setId(1L);
    savedProfile.setUserId(profileDTO.userId());
    savedProfile.setFirstname(profileDTO.firstname());
    savedProfile.setLastname(profileDTO.lastname());
    savedProfile.setPhoneNumber(profileDTO.phoneNumber());

    when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

    // When: saveProfile() is called
    Profile result = profileService.saveProfile(profileDTO);

    // Then: The saved profile should be returned correctly
    assertNotNull(result);
    assertEquals(savedProfile.getId(), result.getId());
    verify(profileRepository).save(any(Profile.class));
  }
}
