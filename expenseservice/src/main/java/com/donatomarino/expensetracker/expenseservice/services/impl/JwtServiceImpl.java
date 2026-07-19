package com.donatomarino.expensetracker.expenseservice.services.impl;

import com.donatomarino.expensetracker.expenseservice.services.interfaces.IJwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;

@Service
public class JwtServiceImpl implements IJwtService {

  @Value("${app.jwt.secret}")
  private String secretKey;

  public Long extractUserId(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get("userId", Long.class);
  }

  // Extracts all claims
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  // Generates the signing key
  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
