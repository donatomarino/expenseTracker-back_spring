package com.donatomarino.expensetracker.auth.services;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.dtos.RegisterResponse;
import com.donatomarino.expensetracker.auth.entities.User;
import com.donatomarino.expensetracker.auth.repositories.UserRepository;
import com.donatomarino.expensetracker.auth.services.impl.AuthService;
import com.donatomarino.expensetracker.auth.services.impl.JwtService;
import com.donatomarino.expensetracker.auth.services.interfaces.IJwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private IJwtService jwtService;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthService authService;

  private final User USER_PREPARED = User.builder()
    .id(1L)
    .fullName("Donato Marino")
    .email("donato@test.com")
    .password("password")
    .build();

  @Test
  void shouldLoginSuccessfully() {
    AuthRequest request = new AuthRequest(
      USER_PREPARED.getEmail(),
      "password"
    );
    // Mock the authentication manager to return a successful authentication
    when(authenticationManager.authenticate(any(
      UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));

    // Mock the user repository to return the prepared user
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(USER_PREPARED));

    // Mock the JWT service to return a token
    when(jwtService.generateToken(anyMap(), eq(USER_PREPARED))).thenReturn("mocked-jwt-token");

    // Call the authenticate method
    AuthResponse response = authService.authenticate(request);

    // Verify the response
    assertEquals("mocked-jwt-token", response.access_token());
    assertEquals(USER_PREPARED.getFullName().split(" ")[0], response.user_name());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, times(1)).findByEmail(request.email());

    // Tells Mockito to capture the Map<String, Object> argument passed to the generateToken method
    ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);

    verify(jwtService).generateToken(claimsCaptor.capture(), eq(USER_PREPARED));
    assertEquals(USER_PREPARED.getId(), claimsCaptor.getValue().get("userId"));
  }

  @Test
  void shouldLoginFailWithInvalidCredentials() {
    AuthRequest request = new AuthRequest(
      USER_PREPARED.getEmail(),
      "wrong-password"
    );

    when(authenticationManager.authenticate(any(
      UsernamePasswordAuthenticationToken.class)))
      .thenThrow(new BadCredentialsException("Bad credentials"));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      authService.authenticate(request);
    });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    assertEquals("Invalid email or password", exception.getReason());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

    verifyNoInteractions(userRepository);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldLoginFailWithUserNotFound() {
    AuthRequest request = new AuthRequest(
      "nonexistentuser@email.com",
      "password"
    );

    when(authenticationManager.authenticate(any(
      UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));

    when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
      authService.authenticate(request);
    });

    assertEquals("[Auth] -> [findUserByEmail]: User not found", exception.getMessage());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, times(1)).findByEmail(request.email());
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldRegisterSuccessfully() {
    RegisterRequest request = new RegisterRequest(
      "Donato Marino",
      "donato@test.com",
      "password"
    );

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

    RegisterResponse response = authService.register(request);

    assertEquals("User registered successfully", response.message());

    verify(userRepository, (times(1))).existsByEmail(request.email());
    verify(passwordEncoder, times(1)).encode(request.password());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertEquals("Donato Marino", savedUser.getFullName());
    assertEquals("donato@test.com", savedUser.getEmail());
    assertEquals("encoded-password", savedUser.getPassword());
  }

  @Test
  void shouldRegisterFailWithExistingEmail() {
    RegisterRequest request = new RegisterRequest(
      "Donato Marino",
      "donato@test.com",
      "password"
    );

    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      authService.register(request);
    });

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Email is already in use", exception.getReason());

    verify(userRepository, times(1)).existsByEmail(request.email());

    verifyNoInteractions(passwordEncoder);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldGenerateTokenWithUserIdClaim() {
    when(jwtService.generateToken(anyMap(), eq(USER_PREPARED)))
      .thenReturn("mocked-jwt-token");

    String token = authService.generateToken(USER_PREPARED);

    assertEquals("mocked-jwt-token", token);

    ArgumentCaptor<Map<String, Object>> claimsCaptor =
      ArgumentCaptor.forClass(Map.class);

    verify(jwtService).generateToken(claimsCaptor.capture(), eq(USER_PREPARED));
    assertEquals(USER_PREPARED.getId(), claimsCaptor.getValue().get("userId"));
  }
}
