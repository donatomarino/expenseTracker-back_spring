package com.donatomarino.expensetracker.expenseservice.security.filters;

import com.donatomarino.expensetracker.expenseservice.services.impl.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtServiceImpl jwtService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    // Check if the header not null and starts with Bearer.
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract token
    final String jwt = authHeader.substring(7);

    try {
      final Long userId = jwtService.extractUserId(jwt);

      // Create authentication object
      UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
          userId,
          null,
          Collections.emptyList()
        );

      // Set authentication in context
      SecurityContextHolder.getContext().setAuthentication(authToken);
    } catch(ExpiredJwtException e){
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
      return;
    } catch(JwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Invalid token");
      return;
    }

    filterChain.doFilter(request, response);
  }
}

