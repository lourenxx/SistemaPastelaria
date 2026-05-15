package br.edu.faculdade.sistemapastelaria.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/index.html", "/error").permitAll()
                .requestMatchers("/Usuario/html/login.html", "/Usuario/html/sucesso.html", "/Usuario/css/**", "/Usuario/js/**").permitAll()
                .requestMatchers("/Cliente/html/login.html", "/Cliente/html/cadastro.html", "/Cliente/css/**", "/Cliente/js/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/h2-console").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuarios/login", "/usuarios/login/verificar-codigo").permitAll()
                .requestMatchers(HttpMethod.POST, "/clientes/cadastro", "/clientes/login").permitAll()
                .requestMatchers("/Admin/**").hasRole("INTERNO")
                .requestMatchers("/Cliente/**").hasRole("CLIENTE")
                .requestMatchers("/usuarios/**").hasRole("INTERNO")
                .requestMatchers("/clientes/me/**").hasRole("CLIENTE")
                .requestMatchers("/clientes/**").hasRole("INTERNO")
                .requestMatchers("/produtos/cardapio").hasRole("CLIENTE")
                .requestMatchers("/produtos/**").hasRole("INTERNO")
                .requestMatchers("/pedidos/**").hasRole("INTERNO")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> response.sendError(401))
                .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(403)))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
