package com.clinicapp.backend.config;

import com.clinicapp.backend.service.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Lombok: Constructor injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Spring Security's UserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue to the next filter
            return;
        }

        // 2. Extract JWT token (remove "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 3. Extract username from JWT
            username = jwtService.extractUsername(jwt);

            // 4. Check if username exists and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Load UserDetails from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 6. Validate the token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 7. Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed as we use JWT
                            userDetails.getAuthorities()
                    );
                    // 8. Set details from the request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 9. Update SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the exception (e.g., token expired, malformed, etc.)
            // logger.error("Cannot set user authentication: {}", e);
            // Optionally clear the context if authentication failed due to token issues
            SecurityContextHolder.clearContext();
        }


        // 10. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}