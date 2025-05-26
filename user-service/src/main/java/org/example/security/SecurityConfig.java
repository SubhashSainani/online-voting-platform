package org.example.security;

import org.example.service.UserDetailsServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint authEntryPoint;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    
    public SecurityConfig(JwtAuthEntryPoint authEntryPoint,
                          JwtUtil jwtUtil,
                          UserDetailsServiceImpl userDetailsService) {
        this.authEntryPoint = authEntryPoint;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    Logger logger = LoggerFactory.getLogger(getClass());
    http
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authEntryPoint)
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/register").permitAll()
            .requestMatchers("/actuator/**").permitAll() 
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler((request, response, authentication) -> {
                logger.info("===== LOGIN SUCCESS HANDLER CALLED ======");
                logger.info("Request URL: {}", request.getRequestURL());
                logger.info("Request URI: {}", request.getRequestURI());
                logger.info("Server Name: {}", request.getServerName());
                logger.info("Server Port: {}", request.getServerPort());
                logger.info("Scheme: {}", request.getScheme());

                String originalHost = request.getHeader("X-Forwarded-Host");
                String originalProto = request.getHeader("X-Forwarded-Proto");

                logger.info("X-Forwarded-Host: {}", originalHost);
                logger.info("X-Forwarded-Proto: {}", originalProto);

                if (originalHost != null && originalHost.contains(",")) {
                    originalHost = originalHost.split(",")[0].trim();
                }
                if (originalProto != null && originalProto.contains(",")) {
                    originalProto = originalProto.split(",")[0].trim();
                }

                logger.info("Cleaned Host: {}", originalHost);
                logger.info("Cleaned Proto: {}", originalProto);

                String token = jwtUtil.generateToken(
                    authentication.getName(),
                    authentication.getAuthorities().iterator().next().getAuthority()
                );

                String baseUrl;
                if (originalHost != null && !originalHost.isEmpty()) {
                    String scheme = originalProto != null && !originalProto.isEmpty() ? originalProto : "http";
                    baseUrl = scheme + "://" + originalHost;
                } else {
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();

                    baseUrl = (("http".equals(scheme) && serverPort == 80) || 
                               ("https".equals(scheme) && serverPort == 443)) 
                               ? scheme + "://" + serverName 
                               : scheme + "://" + serverName + ":" + serverPort;
                }

                String redirectUrl = baseUrl + "/voting/dashboard?token=" + token;
                logger.info("Final redirect URL: {}", redirectUrl);
                logger.info("===== END LOGIN SUCCESS HANDLER =====");

                response.sendRedirect(redirectUrl);
            })
            .failureHandler((request, response, exception) -> {
                logger.warn("===== LOGIN FAILURE HANDLER CALLED =====");
                logger.warn("Error: {}", exception.getMessage());

                String originalHost = request.getHeader("X-Forwarded-Host");
                String originalProto = request.getHeader("X-Forwarded-Proto");

                if (originalHost != null && originalHost.contains(",")) {
                    originalHost = originalHost.split(",")[0].trim();
                }
                if (originalProto != null && originalProto.contains(",")) {
                    originalProto = originalProto.split(",")[0].trim();
                }

                String baseUrl;
                if (originalHost != null && !originalHost.isEmpty()) {
                    String scheme = originalProto != null && !originalProto.isEmpty() ? originalProto : "http";
                    baseUrl = scheme + "://" + originalHost;
                } else {
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();

                    baseUrl = (("http".equals(scheme) && serverPort == 80) || 
                               ("https".equals(scheme) && serverPort == 443)) 
                               ? scheme + "://" + serverName 
                               : scheme + "://" + serverName + ":" + serverPort;
                }

                String failureUrl = baseUrl + "/login?error=true";
                logger.warn("Failure redirect URL: {}", failureUrl);
                response.sendRedirect(failureUrl);
            })
            .permitAll()
        )
        .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessHandler((request, response, authentication) -> {
            logger.info("===== LOGOUT SUCCESS HANDLER CALLED =====");
            
            // Handle forwarded headers for logout redirect
            String originalHost = request.getHeader("X-Forwarded-Host");
            String originalProto = request.getHeader("X-Forwarded-Proto");
            
            logger.info("Logout X-Forwarded-Host: " + originalHost);
            logger.info("Logout X-Forwarded-Proto: " + originalProto);
            
            // Handle comma-separated values
            if (originalHost != null && originalHost.contains(",")) {
                originalHost = originalHost.split(",")[0].trim();
            }
            if (originalProto != null && originalProto.contains(",")) {
                originalProto = originalProto.split(",")[0].trim();
            }
            
            String baseUrl;
            if (originalHost != null && !originalHost.isEmpty()) {
                String scheme = originalProto != null && !originalProto.isEmpty() ? originalProto : "http";
                baseUrl = scheme + "://" + originalHost;
            } else {
                // Fallback to request details
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                
                if ((scheme.equals("http") && serverPort == 80) || 
                    (scheme.equals("https") && serverPort == 443)) {
                    baseUrl = scheme + "://" + serverName;
                } else {
                    baseUrl = scheme + "://" + serverName + ":" + serverPort;
                }
            }
            
            String logoutUrl = baseUrl + "/login?logout";
            logger.info("Logout redirect URL: " + logoutUrl);
            response.sendRedirect(logoutUrl);
        })
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID")
        .clearAuthentication(true)
        .permitAll()
);

    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }
}