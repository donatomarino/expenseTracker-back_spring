package com.donatomarino.expensetracker.auth.security.filter;

import com.donatomarino.expensetracker.auth.services.impl.JwtService;
import com.donatomarino.expensetracker.auth.services.interfaces.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final IJwtService jwtService;
  private final UserDetailsService userDetailsService;

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
      final String userEmail = jwtService.getUsername(jwt);

      // Check that user is not already authenticated
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // Load user from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Validate token
        if (jwtService.isTokenValid(jwt, userDetails)) {
          // Create authentication object
          UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
            );

          // Add request details
          authToken.setDetails(
            new WebAuthenticationDetails(request)
          );

          // Set authentication in context
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

    } catch (ExpiredJwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Token expired");
      return;
    } catch (JwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Invalid token");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
