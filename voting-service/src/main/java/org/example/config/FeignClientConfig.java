package org.example.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            try {
                // Get the current HTTP request
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attributes.getRequest();

                String authHeader = null;

                // Method 1: Try to get token from Authorization header first
                authHeader = request.getHeader("Authorization");
                System.out.println("Feign - Authorization header from request: " + authHeader);

                // Method 2: If no header, try to get from request parameter
                if (authHeader == null || authHeader.isEmpty()) {
                    String tokenParam = request.getParameter("token");
                    System.out.println("Feign - Token parameter from request: " + tokenParam);
                    if (tokenParam != null && !tokenParam.isEmpty()) {
                        authHeader = "Bearer " + tokenParam;
                    }
                }

                // Method 3: Try to get from Security Context (if user is authenticated)
                if (authHeader == null || authHeader.isEmpty()) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        // If we have an authenticated user but no token, we might need to generate one
                        // For now, let's just log this case
                        System.out.println("Feign - User authenticated but no token found: " + authentication.getName());
                    }
                }

                // Method 4: Check for token in request attributes (might be set by JWT filter)
                if (authHeader == null || authHeader.isEmpty()) {
                    String tokenFromAttr = (String) request.getAttribute("token");
                    System.out.println("Feign - Token from request attributes: " + tokenFromAttr);
                    if (tokenFromAttr != null && !tokenFromAttr.isEmpty()) {
                        authHeader = "Bearer " + tokenFromAttr;
                    }
                }

                // If we have an auth header, add it to the Feign request
                if (authHeader != null && !authHeader.isEmpty()) {
                    System.out.println("Feign - Adding Authorization header: " + authHeader);
                    requestTemplate.header("Authorization", authHeader);
                } else {
                    System.out.println("Feign - No authorization token found for request");
                }

            } catch (Exception e) {
                System.err.println("Feign - Error in request interceptor: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}