package com.donato.habitly.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
  @NotBlank(message = "Email required")
  @Email(message = "Invalid email format")
  String email,

  @NotBlank(message = "Password required")
  String password
) {
}
