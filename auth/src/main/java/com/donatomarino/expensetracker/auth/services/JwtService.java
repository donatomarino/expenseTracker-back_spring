package com.donatomarino.expensetracker.auth.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secretKey;

  @Value("${app.jwt.expiration:86400000}")
  private long jwtExpiration;

  // Generate token with custom claims
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
      .claims(extraClaims)
      .subject(userDetails.getUsername())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
      .signWith(getSigningKey(), Jwts.SIG.HS256)
      .compact();
  }

  public String getUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // Extract a specific claim
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // Validates the token
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = getUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  // Checks if the token has expired
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
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
