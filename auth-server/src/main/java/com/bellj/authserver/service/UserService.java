package com.bellj.authserver.service;

import com.bellj.authserver.entity.User;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.model.UpstreamProfileRequest;
import com.bellj.authserver.repository.UserRepository;
import java.net.URI;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service class responsible for managing the CRUD functionality over User entities. */
@Service
public class UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  private final ResourceServerService resourceServerService;

  public UserService(UserRepository userRepository, ResourceServerService resourceServerService) {
    this.userRepository = userRepository;
    this.resourceServerService = resourceServerService;
  }

  /**
   * Validates a user based on username and password.
   *
   * @param username The username to look for.
   * @param password The plaintext password to check.
   * @return An optional containing the User ID if credentials match.
   */
  public Optional<Long> getValidUserId(String username, String password) {
    return Optional.ofNullable(userRepository.findByUsername(username))
        .filter(user -> BCrypt.checkpw(password, user.getPassword()))
        .map(
            user -> {
              LOGGER.info("User authenticated successfully: {}", user.getId());
              return user.getId();
            });
  }

  /**
   * Handles the creation of a new User object, including password hashing.
   *
   * @param registerRequest The inbound HTTP request body.
   * @return The outcome of the user creation. True for success, false for failure.
   */
  public boolean createUser(RegisterRequest registerRequest) {
    User user = new User();
    user.setUsername(registerRequest.username());
    user.setPassword(BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt(12)));

    User savedUser = userRepository.save(user);
    if (savedUser.getId() == null) {
      LOGGER.error("User creation failed.");
      return false;
    }

    LOGGER.info("User created successfully: {}", savedUser.getId());

    UpstreamProfileRequest upstreamProfileRequest = new UpstreamProfileRequest();
    upstreamProfileRequest.setUserId(savedUser.getId());
    upstreamProfileRequest.setFirstname(registerRequest.firstname());
    upstreamProfileRequest.setLastname(registerRequest.lastname());
    upstreamProfileRequest.setPhoneNumber(registerRequest.phoneNumber());

    URI uri = resourceServerService.createUserProfile(upstreamProfileRequest);
    LOGGER.info("Profile creation response: {}", uri);

    return true;
  }
}
