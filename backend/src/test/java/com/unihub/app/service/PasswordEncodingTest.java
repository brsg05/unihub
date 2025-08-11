package com.unihub.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncodingTest {

    @Test
    void bCrypt_encoder_matches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String raw = "secret123";
        String hash = encoder.encode(raw);
        assertTrue(encoder.matches(raw, hash));
        assertFalse(encoder.matches("wrong", hash));
    }
}
