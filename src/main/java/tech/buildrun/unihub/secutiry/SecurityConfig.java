package tech.buildrun.unihub.secutiry;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração principal do Spring Security.
 * Define a cadeia de filtros de segurança, provedores de autenticação,
 * regras de autorização e configuração de CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita segurança baseada em anotações (@PreAuthorize)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Define a cadeia de filtros de segurança HTTP.
     * Configura CORS, CSRF, gerenciamento de sessão, regras de autorização e o filtro JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita e configura CORS
                .csrf(AbstractHttpConfigurer::disable) // Desabilita CSRF para APIs RESTful
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso público para endpoints de autenticação
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Permite acesso público para endpoints de professores e comentários (visualização)
                        .requestMatchers("/api/v1/professors/**").permitAll()
                        .requestMatchers("/api/v1/comments/**").permitAll()
                        // Permite acesso público para a documentação do Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // Permite acesso público para endpoints do Actuator (health check)
                        .requestMatchers("/actuator/health").permitAll()
                        // Todas as outras requisições requerem autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sessão stateless (sem estado) para JWT
                )
                .authenticationProvider(authenticationProvider()) // Define o provedor de autenticação
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Adiciona o filtro JWT

        return http.build();
    }

    /**
     * Configura o provedor de autenticação.
     * Utiliza DaoAuthenticationProvider com UserDetailsService e BCryptPasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Gerenciador de autenticação.
     * @param config Configuração de autenticação.
     * @return Instância do AuthenticationManager.
     * @throws Exception se houver erro na configuração.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean para o codificador de senhas (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuração de CORS (Cross-Origin Resource Sharing).
     * Permite requisições de qualquer origem, com métodos e cabeçalhos específicos.
     * Em produção, isso deve ser mais restritivo.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Permitir todas as origens (em produção, especificar domínios)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(false); // Não permitir credenciais (cookies, etc.)
        configuration.setMaxAge(3600L); // Tempo máximo para cache de preflight requests

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar a todas as rotas
        return source;
    }
}
