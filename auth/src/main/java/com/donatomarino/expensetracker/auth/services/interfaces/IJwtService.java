package com.donatomarino.expensetracker.auth.services.interfaces;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface IJwtService {
  String generateToken(Map<String, Object> claims, UserDetails userDetails);
  String getUsername(String token);
  <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    ;
  boolean isTokenValid(String token, UserDetails userDetails);
}
