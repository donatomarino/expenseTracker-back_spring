package com.donatomarino.expensetracker.auth.dtos;

public record AuthResponse(
  String access_token,
  String token_type,
  String user_name,
  long expiresIn
) {
  public AuthResponse(String access_token, String user_name, long expiresIn) {
    this(access_token, "Bearer", user_name, expiresIn);
  }
}
