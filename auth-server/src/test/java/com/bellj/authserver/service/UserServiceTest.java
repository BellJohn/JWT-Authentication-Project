package com.bellj.authserver.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bellj.authserver.entity.User;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.model.UpstreamProfileRequest;
import com.bellj.authserver.repository.UserRepository;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private ResourceServerService resourceServerService;

  @InjectMocks private UserService userService;

  @Test
  void testGetValidUserId_ValidCredentials() {
    // Given
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt(12)));
    ReflectionTestUtils.setField(testUser, "id", 1L);

    when(userRepository.findByUsername("testUser")).thenReturn(testUser);

    // When
    Optional<Long> userId = userService.getValidUserId("testUser", "password123");

    // Then
    assertTrue(userId.isPresent());
    assertEquals(1L, userId.get());
    verify(userRepository).findByUsername("testUser");
  }

  @Test
  void testGetValidUserId_InvalidCredentials() {
    // Given
    User mockUser = new User();
    mockUser.setUsername("testUser");
    mockUser.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt(12)));

    when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

    // When
    Optional<Long> userId = userService.getValidUserId("testUser", "wrongPassword");

    // Then
    assertFalse(userId.isPresent());
    verify(userRepository).findByUsername("testUser");
  }

  @Test
  void testCreateUser_Success() {
    // Given
    RegisterRequest request =
        new RegisterRequest("testUser", "password123", "John", "Doe", "123456789");
    User testUser = new User();
    ReflectionTestUtils.setField(testUser, "id", 1L);
    testUser.setUsername(request.username());
    testUser.setPassword(BCrypt.hashpw(request.password(), BCrypt.gensalt(12)));

    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(resourceServerService.createUserProfile(any(UpstreamProfileRequest.class)))
        .thenReturn(URI.create("http://resource-server/profile/1"));

    // When
    boolean isCreated = userService.createUser(request);

    // Then
    assertTrue(isCreated);
    verify(userRepository).save(any(User.class));
    verify(resourceServerService).createUserProfile(any(UpstreamProfileRequest.class));
  }

  @Test
  void testCreateUser_Failure() {
    // Given
    RegisterRequest request =
        new RegisterRequest("testUser", "password123", "John", "Doe", "123456789");
    User mockUser = new User(); // User without an ID simulates a failed save operation.

    when(userRepository.save(any(User.class))).thenReturn(mockUser);

    // When
    boolean isCreated = userService.createUser(request);

    // Then
    assertFalse(isCreated);
    verify(userRepository).save(any(User.class));
    verify(resourceServerService, never()).createUserProfile(any(UpstreamProfileRequest.class));
  }
}
