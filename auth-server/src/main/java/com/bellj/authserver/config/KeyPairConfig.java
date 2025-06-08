package com.bellj.authserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyPairConfig {

  /**
   * Generally not a good idea. This should be loading pre-generated keys from a keystore.
   * Otherwise, your application will have different keys each boot. If you scaled this
   * horizontally, you would end up with different keypairs on each node. A real production system
   * should have a mechanism for managing the keypair lifecycle externally.
   *
   * @return An RSA pub/priv keypair
   */
  @Bean
  public KeyPair getKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.genKeyPair();
  }
}
