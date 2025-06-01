package com.bellj.authserver.service;

import com.bellj.authserver.entity.User;
import com.bellj.authserver.model.UpstreamProfileRequest;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public final UserRepository userRepository;
    private final ResourceServerService resourceServerService;

    public UserService(UserRepository userRepository, ResourceServerService resourceServerService) {
        this.userRepository = userRepository;
        this.resourceServerService = resourceServerService;
    }

    public Optional<Long> getValidUserId(String username, String password) {
        User user = userRepository.findByUsername(username);
        LOGGER.info("Found user: {}", user);

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            LOGGER.info("Password matches, returning user ID: {}", user.getId());
            return Optional.ofNullable(user.getId());
        }
        LOGGER.info("Invalid credentials, returning empty Optional");
        return Optional.empty();
    }

    public boolean createUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt(12)));
        User savedUser = userRepository.save(user);

        UpstreamProfileRequest upstreamProfileRequest = new UpstreamProfileRequest();
        upstreamProfileRequest.setUserId(savedUser.getId());
        upstreamProfileRequest.setFirstname(registerRequest.firstname());
        upstreamProfileRequest.setLastname(registerRequest.lastname());
        upstreamProfileRequest.setPhoneNumber(registerRequest.phoneNumber());
        URI uri = resourceServerService.createUserProfile(upstreamProfileRequest);
        if (uri != null) {
            LOGGER.info("Returned URI: [{}]", uri);
        }

        return savedUser.getId() != null;
    }
}
