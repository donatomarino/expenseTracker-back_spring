package com.donatomarino.expensetracker.auth.dtos;

import com.donatomarino.expensetracker.auth.entities.User;

public record RegisterResponse(
  String message,
  boolean success
) {
}
