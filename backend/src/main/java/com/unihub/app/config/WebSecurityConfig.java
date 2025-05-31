package com.unihub.app.config;

import com.unihub.app.security.jwt.AuthEntryPointJwt;
import com.unihub.app.security.jwt.AuthTokenFilter;
import com.unihub.app.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/professores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cadeiras/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/criterios/**").permitAll()
                // Avaliacoes related public GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/avaliacoes/professores/**").permitAll() // e.g. /api/avaliacoes/professores/{profId}/cadeiras/{cadId}
                .requestMatchers(HttpMethod.GET, "/api/criterios/{criterioId}/professores/{professorId}").permitAll()
                
                // User specific actions (Avaliar, Votar em comentário)
                .requestMatchers(HttpMethod.POST, "/api/avaliacoes").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/comentarios/{comentarioId}/vote").hasRole("USER")

                // Admin actions
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/professores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/professores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/professores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/cadeiras/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/cadeiras/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/cadeiras/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/criterios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/criterios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/criterios/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
} 