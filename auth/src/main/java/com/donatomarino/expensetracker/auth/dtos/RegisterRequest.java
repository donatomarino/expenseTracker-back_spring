package com.donatomarino.expensetracker.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank(message="Full name required")
  @Size(min=3, message = "Full name must be at least 3 characters")
  String fullName,

  @NotBlank(message="Email required")
  String email,

  @NotBlank(message = "Password required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  String password
) { }
