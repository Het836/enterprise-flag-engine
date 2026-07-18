package com.enterprise.backend.security;

import com.enterprise.backend.entity.Environment;
import com.enterprise.backend.repository.EnvironmentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final EnvironmentRepository environmentRepository;
    private static final String API_KEY_HEADER = "X-Environment-API-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Target only SDK evaluation and environment-scoped endpoints
        if(requestURI.startsWith("/api/v1/sdk") || requestURI.startsWith("/api/v1/environment")){
            String apiKey = request.getHeader(API_KEY_HEADER);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Missing API Key in header (" + API_KEY_HEADER + ")\"}");
                return;
            }

            // Validate against the database string via EnvironmentRepository
            Optional<Environment> environment = environmentRepository.findByApiKey(apiKey);

            if (environment.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid or unauthorized Environment API Key\"}");
                return;
            }

            // Attach the validated environment data to the request attributes for downstream use if needed
            request.setAttribute("VALIDATED_ENVIRONMENT", environment.get());
        }
        filterChain.doFilter(request,response);
    }
}
