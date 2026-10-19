package com.donatomarino.expensetracker.auth.controllers;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.dtos.RegisterResponse;
import com.donatomarino.expensetracker.auth.services.AuthService;
import com.donatomarino.expensetracker.auth.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtService jwtService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String asJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldRegisterSuccessfully() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest(
      "Donato Marino",
      "donato@icloud.com",
      "password"
    );

    when(authService.register(any(RegisterRequest.class))).thenReturn(new RegisterResponse("User registered successfully", true));

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(registerRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("User registered successfully"))
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void shouldReturn400WhenRegisterRequestIsInvalid() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest(
      "Donato",
      "invalid-email",
      ""
    );

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(registerRequest)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenEmailIsAlreadyInUse() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest(
      "Donato Marino",
      "donato@test.com",
      "password"
    );

    when(authService.register(any(RegisterRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use"));

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(registerRequest)))
      .andExpect(status().isConflict());
  }

  @Test
  void shouldLoginSuccessfully() throws Exception {
    AuthRequest authRequest = new AuthRequest(
      "donato@test.com",
      "password"
    );

    when(authService.authenticate(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt-token", "D", 86400000));

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(authRequest))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").value("jwt-token"))
      .andExpect(jsonPath("$.user_name").value("D"))
      .andExpect(jsonPath("$.expiresIn").value(86400000));
  }

  @Test
  void shouldReturn401WhenLoginFails() throws Exception {
    AuthRequest authRequest = new AuthRequest(
      "nonexistentuser@email.com",
      "password"
    );

    when(authService.authenticate(any(AuthRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(authRequest))
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn400WhenLoginRequestIsInvalid() throws Exception {
    AuthRequest authRequest = new AuthRequest(
      "invalid-email",
      ""
    );

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(authRequest))
      )
      .andExpect(status().isBadRequest());
  }
}
