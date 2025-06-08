package com.bellj.authserver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bellj.authserver.model.LoginRequest;
import com.bellj.authserver.model.RegisterRequest;
import com.bellj.authserver.service.TokenService;
import com.bellj.authserver.service.UserService;
import com.nimbusds.jose.JOSEException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private AuthController authController;

  @Mock private TokenService mockTokenService;

  @Mock private UserService mockUserService;

  @BeforeEach
  void beforeEach() {
    Mockito.reset(mockTokenService, mockUserService);
    authController = new AuthController(mockTokenService, mockUserService);
  }

  @Test
  void testLoginSuccess() throws JOSEException {
    // Given
    LoginRequest loginRequest = new LoginRequest("testUsername", "testPassword");
    when(mockUserService.getValidUserId("testUsername", "testPassword"))
        .thenReturn(Optional.of(1L));
    when(mockTokenService.generateUserToken(1L)).thenReturn("Signed JWT String");

    // When
    ResponseEntity<String> response = authController.login(loginRequest);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Signed JWT String", response.getBody());
  }

  @Test
  void testRegisterUserSuccess() {
    // Given
    RegisterRequest registerRequest =
        new RegisterRequest("testUsername", "testPassword", "first", "last", "01234567891");
    when(mockUserService.createUser(registerRequest)).thenReturn(true);

    // When
    ResponseEntity<String> response = authController.registerUser(registerRequest);

    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("User Registered OK", response.getBody());
  }

  @Test
  void testGetJWKSSuccess() throws NoSuchAlgorithmException {
    // Given
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    PublicKey publicKey = keyGen.genKeyPair().getPublic();
    when(mockTokenService.getPublicKey()).thenReturn(publicKey);

    // When
    ResponseEntity<String> response = authController.getJWKS();

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
