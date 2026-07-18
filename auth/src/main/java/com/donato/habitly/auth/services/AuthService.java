package com.donato.habitly.auth.services;

import com.donato.habitly.auth.dtos.AuthRequest;
import com.donato.habitly.auth.dtos.AuthResponse;
import com.donato.habitly.auth.dtos.RegisterRequest;
import com.donato.habitly.auth.entities.User;
import com.donato.habitly.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Value("${app.jwt.expiration:86400000}")
  private long jwtExpiration;

  /**
   * Registers a new user
   *
   * @param request
   * @return JWT token
   */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check that email is not already in use
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("[Auth] -> [register]: Email is already in use");
    }

    // Create user
    User user = new User();
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));

    userRepository.save(user);

    // Generate token
    String jwtToken = generateToken(user);
    return new AuthResponse(jwtToken, jwtExpiration);
  }

  /**
   * Authenticates a user
   *
   * @param request
   * @return JWT token
   */
  public AuthResponse authenticate(AuthRequest request) {
    // Delegate verification to AuthenticationManager
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.email(),
        request.password()
      )
    );

    User user = findUserByEmail(request.email());

    // Generate token
    String jwtToken = generateToken(user);

    return new AuthResponse(jwtToken, jwtExpiration);
  }

  /**
   * Refresh token
   *
   * @param authHeader
   * return new JWT token
   */
  public AuthResponse refreshToken(String authHeader) {
    // Check that the header is valid
    if(authHeader == null ||!authHeader.startsWith("Bearer ")) {
      throw new BadCredentialsException("[Auth] -> [refreshToken]: Invalid token");
    }

    String oldToken = authHeader.substring(7);
    String userEmail = jwtService.getUsername(oldToken);

    User user = findUserByEmail(userEmail);

    // Generate new token
    String newToken = generateToken(user);
    return new AuthResponse(newToken, jwtExpiration);
  }

  /**
   * Find user by email
   *
   * @param email
   * @return User
   */
  public User findUserByEmail(String email) {
    return userRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException("[Auth] -> [findUserByEmail]: User not found"));
  }

  /**
   * Generate JWT token
   *
   * @param user
   * @return JWT token
   */
  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());

    return jwtService.generateToken(claims, user);
  }

}
