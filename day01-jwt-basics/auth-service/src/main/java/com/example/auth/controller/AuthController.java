package com.example.auth.controller;


import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.*;
import com.example.auth.model.ConfirmationCode;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import org.apache.coyote.Response;
import org.hibernate.query.NativeQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")


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
    public  ResponseEntity<ApiResponse<RegisterResponce>> register (@RequestBody RegisterRequest request){
        String username = authService.register(request.getUsername(),request.getPassword(),request.getEmail());
        RegisterResponce registerResponce = new RegisterResponce();
        registerResponce.setUsername(request.getUsername());
        registerResponce.setPassword(request.getPassword());
        return  ResponseEntity.ok(ApiResponse.success(registerResponce));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login (@RequestBody LoginRequest request){

        TokenResponse  tokenResponse = authService.login(request.getUsername(),request.getPassword());
        return  ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Map<String,String>>> confirm(@RequestBody ConfirmRequest request){
        authService.confirm(request.getUsername(),request.getCode());

        Map<String,String>  result = new HashMap<>();
        result.put("message","user seccessfully confirmed");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Map<String,String>>> resend (@RequestBody ResendRequest resendRequest){
        String email = resendRequest.getEmail();
        authService.sendCodeAgain(email);
        Map<String,String>  result = new HashMap<>();
        result.put("message","code to "+ email + "was seccessfully send");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/refresh")
    public  ResponseEntity<ApiResponse> refresh(@RequestBody RefreshRequest request){
       TokenResponse tokenResponse =  authService.refresh(request.getRefreshToken());
       return  ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }
    @PostMapping("/logout")
    public  ResponseEntity<ApiResponse<Void>> logout (@RequestBody  RefreshRequest request){
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));

    }





}

