package com.example.auth.config;

import com.example.auth.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private  final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig( JwtAuthFilter jwtAuthFilter){
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public    SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/login",
                        "/swagger-ui/**", "/v3/api-docs/**").permitAll()  // открыть для всех
                        .anyRequest().authenticated()

        ).sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) ) // без сессий (только JWT)
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//        //                            вот здесь я сказал что поставь мой фильтр

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

// работает так что  есть класс SecurityConfig и который принимает через конструткор параметр
//jwtAuthFilter это наш класс который является фильторм и потом в функции  SecurityFilterChain
// мы говорим что наш http обьект будет без csrf ( это защита от куки хакеров )
// что мол эндпоинты (api/register api/login) свободны для всех и в них может стучаться  любой
// говорим что  все остальные эндпоинты доступны только тем кто авторизован
// говорим что мы не используем сесси а действуем по принципу дали запрос - дал ответ - забил болт
// потом перед фильтром спринга мы ставим наш собственный филльтр на JWT


