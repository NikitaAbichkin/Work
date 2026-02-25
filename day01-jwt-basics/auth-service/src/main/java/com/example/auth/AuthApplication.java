package com.example.auth;
import com.example.auth.service.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class  AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);

    }
    @Bean
    public CommandLineRunner testTokenGeneration(JwtService jwtService){

       return args -> {
           String token = jwtService.generateToken("Nikita", 1L);
           System.out.println("Сгенерированный токен: " + token);

           String username = jwtService.extractUsername(token);
           System.out.println("Имя из токена: " + username);

           Boolean isValid = jwtService.validateToken(token);
           System.out.println(isValid);

       };
    }
}

