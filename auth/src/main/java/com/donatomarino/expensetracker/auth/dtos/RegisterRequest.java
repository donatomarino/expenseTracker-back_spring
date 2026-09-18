package com.donatomarino.expensetracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @JsonProperty("full_name")
  @NotBlank(message = "Full name required")
  @Size(min = 3, max = 64, message = "Full name must be between 3 and 64 characters")
  @Pattern(regexp = "^[\\p{L}]+(?:[ '-][\\p{L}]+)+$", message = "Full name must contain first name and last name")
  String fullName,

  @NotBlank(message = "Email required")
  @Email(message = "Invalid email format")
  @Size(max = 254, message = "Email must not exceed 254 characters")
  String email,

  @NotBlank(message = "Password required")
  @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
  String password
) {
}
