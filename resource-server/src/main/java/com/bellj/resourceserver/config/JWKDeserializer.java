package com.bellj.resourceserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWKDeserializer extends JsonDeserializer<JWK> {

    @Override
    public JWK deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        // Read JSON tree
        JsonNode node = parser.getCodec().readTree(parser);

        // Identify key type based on the "kty" field (Key Type)
        String keyType = node.get("kty").asText();

        return switch (keyType) {
            case "RSA" -> parseRSAKey(node);
            default -> throw new IOException("Unsupported JWK key type: " + keyType);
        };
    }


    private RSAKey parseRSAKey(JsonNode node) {
        return new RSAKey.Builder(new Base64URL(node.get("n").asText()),  // Modulus
                new Base64URL(node.get("e").asText())   // Public exponent
        ).keyID(node.has("kid") ? node.get("kid").asText() : null) // Key ID
                .build();
    }

    private OctetSequenceKey parseSecretKey(JsonNode node) {
        return new OctetSequenceKey.Builder(new Base64URL(node.get("k").asText())) // Symmetric key material
                .keyID(node.has("kid") ? node.get("kid").asText() : null).build();
    }
}

