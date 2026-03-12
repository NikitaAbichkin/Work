package com.example.auth.controller;


import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.*;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
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
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestBody RegisterRequest request) {
        log.info("HTTP POST /api/auth/register for username={} email={}", request.getUsername(), request.getEmail());
        String username = authService.register(request.getUsername(), request.getPassword(), request.getEmail());
        String gmail = request.getEmail();
        Map<String, String> result = new HashMap<>();
        result.put("username", gmail);
        result.put("message", "Письмо с кодом подтверждения отправлено на email");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login (@RequestBody LoginRequest request){
        log.info("HTTP POST /api/auth/login for username={}", request.getUsername());
        TokenResponse  tokenResponse = authService.login(request.getUsername(),request.getPassword());
        return  ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Map<String,String>>> confirm(@RequestBody ConfirmRequest request){
        log.info("HTTP POST /api/auth/confirm for username={}", request.getUsername());
        authService.confirm(request.getUsername(),request.getCode());

        Map<String,String>  result = new HashMap<>();
        result.put("message","user seccessfully confirmed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Map<String,String>>> resend (@RequestBody ResendRequest resendRequest){
        String email = resendRequest.getEmail();
        log.info("HTTP POST /api/auth/resend for email={}", email);
        authService.sendCodeAgain(email);
        Map<String,String>  result = new HashMap<>();
        result.put("message","code to "+ email + " was seccessfully send");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/refresh")
    public  ResponseEntity<ApiResponse> refresh(@RequestBody RefreshRequest request){
       log.info("HTTP POST /api/auth/refresh");
       TokenResponse tokenResponse =  authService.refresh(request.getRefreshToken());
       return  ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }
    @PostMapping("/logout")
    public  ResponseEntity<ApiResponse<Void>> logout (@RequestBody  RefreshRequest request){
        log.info("HTTP POST /api/auth/logout");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));

    }





}

