package com.donato.habitly.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank(message="First name required")
  String firstName,

  @NotBlank(message = "Last name required")
  String lastName,

  @NotBlank(message="Email required")
  String email,

  @NotBlank(message = "Password required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  String password
) { }
