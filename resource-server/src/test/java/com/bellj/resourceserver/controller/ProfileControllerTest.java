package com.bellj.resourceserver.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bellj.resourceserver.dto.ProfileDTO;
import com.bellj.resourceserver.entity.Profile;
import com.bellj.resourceserver.service.ProfileService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

  @Mock private ProfileService profileService;

  @InjectMocks private ProfileController profileController;

  @BeforeEach
  void setUp() {
    profileController = new ProfileController(profileService);
  }

  @Test
  void testGetProfile_Found() {
    // Given: A profile exists for userId = 123
    Profile mockProfile = new Profile();
    mockProfile.setId(123L);
    when(profileService.getProfile(123L)).thenReturn(Optional.of(mockProfile));

    // When: getProfile() is called
    ResponseEntity<Profile> response = profileController.getProfile("123");

    // Then: The profile should be returned with HTTP 200
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(mockProfile, response.getBody());
  }

  @Test
  void testGetProfile_NotFound() {
    // Given: No profile exists for userId = 999
    when(profileService.getProfile(999L)).thenReturn(Optional.empty());

    // When: getProfile() is called
    ResponseEntity<Profile> response = profileController.getProfile("999");

    // Then: HTTP 404 should be returned
    assertEquals(404, response.getStatusCodeValue());
    assertNull(response.getBody());
  }

  @Test
  void testCreateProfile_Success() {
    // Given: A valid ProfileDTO and a saved profile
    ProfileDTO profileDTO = new ProfileDTO(456L, "first", "last", "01234567891");
    Profile mockProfile = new Profile();
    mockProfile.setId(456L);
    when(profileService.saveProfile(profileDTO)).thenReturn(mockProfile);

    // When: createProfile() is called
    ResponseEntity<Long> response = profileController.createProfile(profileDTO);

    // Then: HTTP 200 should be returned with correct profile ID
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(456L, response.getBody());
  }
}
