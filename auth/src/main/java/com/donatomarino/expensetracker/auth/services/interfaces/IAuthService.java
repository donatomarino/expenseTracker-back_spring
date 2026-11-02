package com.donatomarino.expensetracker.auth.services.interfaces;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.dtos.RegisterResponse;

public interface IAuthService {
  RegisterResponse register(RegisterRequest request);
  AuthResponse authenticate(AuthRequest request);
  AuthResponse refreshToken(String authHeader);
}
