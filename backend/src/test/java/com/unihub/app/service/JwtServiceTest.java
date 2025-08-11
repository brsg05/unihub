package com.unihub.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generateToken_containsSubjectAndClaims_andValidates() {
        JwtService jwtService = new JwtService();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123L);
        claims.put("role", "ROLE_ADMIN");
        claims.put("email", "user@example.com");

        String token = jwtService.generateToken(claims, "user@example.com");
        assertNotNull(token);
        assertFalse(token.isBlank());

        String subject = jwtService.extractUserName(token);
        assertEquals("user@example.com", subject);

        Number userIdNum = jwtService.extractClaim(token, c -> c.get("userId", Number.class));
        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        String email = jwtService.extractClaim(token, c -> c.get("email", String.class));

        assertNotNull(userIdNum);
        assertEquals(123L, userIdNum.longValue());
        assertEquals("ROLE_ADMIN", role);
        assertEquals("user@example.com", email);

        User userDetails = new User("user@example.com", "password", java.util.Collections.emptyList());
        assertTrue(jwtService.validateToken(token, userDetails));

        User other = new User("someone@else.com", "password", java.util.Collections.emptyList());
        assertFalse(jwtService.validateToken(token, other));
    }
}
