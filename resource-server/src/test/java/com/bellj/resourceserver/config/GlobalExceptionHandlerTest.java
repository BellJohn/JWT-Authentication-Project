package com.bellj.resourceserver.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  @Test
  void testHandleGenericException() {
    // Given
    // When
    ResponseEntity<String> result =
        new GlobalExceptionHandler().handleGenericException(new Exception("test"));

    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertEquals("An error occurred: test", result.getBody());
  }
}
