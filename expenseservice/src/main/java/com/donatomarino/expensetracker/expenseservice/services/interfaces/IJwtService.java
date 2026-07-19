package com.donatomarino.expensetracker.expenseservice.services.interfaces;

public interface IJwtService {
  Long extractUserId(String token);
}
