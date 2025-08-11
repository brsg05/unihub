//package com.unihub.app.security.jwt;
//
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.security.SignatureException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//
//@Component
//public class JwtUtils {
//    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//
//    @Value("${app.jwt.secret}")
//    private String jwtSecretString;
//
//    @Value("${app.jwt.expirationMs}")
//    private int jwtExpirationMs;
//
//    private SecretKey key() {
//        // Ensure the key is strong enough for HS512
//        byte[] secretBytes = jwtSecretString.getBytes();
//        if (secretBytes.length < 64) { // 512 bits / 8 bits/byte = 64 bytes
//            // This is a simplified approach; in production, ensure your secret is sufficiently long and securely managed.
//            // Pad or use a key derivation function if necessary, or simply use a longer secret.
//            logger.warn("JWT Secret is too short for HS512. It should be at least 64 bytes long. Using it as is, but this is insecure.");
//        }
//        return Keys.hmacShaKeyFor(secretBytes);
//    }
//
//    public String generateJwtToken(Authentication authentication) {
//        UserDetails userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
//
//        return Jwts.builder()
//                .setSubject((userPrincipal.getUsername()))
//                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//                .signWith(key(), SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    public String getUserNameFromJwtToken(String token) {
//        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
//    }
//
//    public boolean validateJwtToken(String authToken) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
//            return true;
//        } catch (SignatureException e) {
//            logger.error("Invalid JWT signature: {}", e.getMessage());
//        } catch (MalformedJwtException e) {
//            logger.error("Invalid JWT token: {}", e.getMessage());
//        } catch (ExpiredJwtException e) {
//            logger.error("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            logger.error("JWT token is unsupported: {}", e.getMessage());
//        } catch (IllegalArgumentException e) {
//            logger.error("JWT claims string is empty: {}", e.getMessage());
//        }
//        return false;
//    }
//}