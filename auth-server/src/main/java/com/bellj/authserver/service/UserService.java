package com.bellj.authserver.service;

import com.bellj.authserver.controller.AuthController;
import com.bellj.authserver.entity.User;
import com.bellj.authserver.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
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

    public boolean saveUser(User user) {
        User savedUser = userRepository.save(user);
         return savedUser.getId() != null;
    }
}
