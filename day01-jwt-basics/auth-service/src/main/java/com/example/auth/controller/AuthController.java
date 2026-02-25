package com.example.auth.controller;


import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.*;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")


public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController // дефолтный конструктор
            (AuthService authService, JwtService jwtService,UserRepository userRepository){
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public  ResponseEntity<Map<String,String>> register (@RequestBody RegisterRequest request){
        String username = authService.register(request.getUsername(),request.getPassword());
        Map<String,String> response  = new HashMap<>();
        response.put("username", username);
        response.put("message", "User registered successfully");

        return ResponseEntity.ok(response);

    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login (@RequestBody LoginRequest request){
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

}

