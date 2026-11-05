package com.donatomarino.expensetracker.auth.controllers;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.dtos.RegisterResponse;
import com.donatomarino.expensetracker.auth.services.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final IAuthService authService;

  /**
   * Registro de un nuevo usuario y generación de token JWT
   *
   * @param request
   * @return ResponseEntity con el token JWT generado para el nuevo usuario o un error si el mail ya está en uso
   */
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
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

  // /**
  //  * Refrescar el token y crear nuevo
  //  *
  //  * @param authHeader
  //  * @return ResponseEntity con el nuevo token JWT o un error si el token es inválido o ha expirado
  //  */
  // @PostMapping("/refresh")
  // public ResponseEntity<AuthResponse> refresh(
  //   @RequestHeader("Authorization") String authHeader
  // ) {
  //   return ResponseEntity.ok(this.authService.refreshToken(authHeader));
  // }

}
