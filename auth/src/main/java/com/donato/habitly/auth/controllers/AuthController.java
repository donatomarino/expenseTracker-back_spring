package com.donato.habitly.auth.controllers;

import com.donato.habitly.auth.dtos.AuthRequest;
import com.donato.habitly.auth.dtos.AuthResponse;
import com.donato.habitly.auth.dtos.RegisterRequest;
import com.donato.habitly.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * Registro de un nuevo usuario y generación de token JWT
   *
   * @param request
   * @return ResponseEntity con el token JWT generado para el nuevo usuario o un error si el mail ya está en uso
   */
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(this.authService.register(request));
  }

  /**
   * Autenticación de usuario y generación de token JWT
   *
   * @param request
   * @return ResponseEntity con el token JWT o un error si las credenciales son inválidas
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(this.authService.authenticate(request));
  }

  /**
   * Refrescar el token y crear nuevo
   *
   * @param authHeader
   * @return ResponseEntity con el nuevo token JWT o un error si el token es inválido o ha expirado
   */
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
    @RequestHeader("Authorization") String authHeader
  ) {
    return ResponseEntity.ok(this.authService.refreshToken(authHeader));
  }

}
