package com.example.auth;

import com.example.auth.dto.TokenResponse;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    // ✅ Тест 1: Регистрация — успех
    @Test
    void register_success() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
                .thenReturn("testuser");

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "testuser",
                "password", "secret123",
                "email", "test@example.com"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    // ✅ Тест 2: Регистрация — username уже занят
    @Test
    void register_usernameAlreadyExists() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Username already exists"));

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "testuser",
                "password", "secret123",
                "email", "test@example.com"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    // ✅ Тест 3: Логин — успех
    @Test
    void login_success() throws Exception {
        TokenResponse fakeToken = new TokenResponse("access-jwt-token", "refresh-uuid-token");
        when(authService.login(anyString(), anyString())).thenReturn(fakeToken);

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "testuser",
                "password", "secret123"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.access_token").value("access-jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-uuid-token"));
    }

    // ✅ Тест 4: Логин — аккаунт не подтверждён
    @Test
    void login_notConfirmed() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Аккаунт не активирован. Подтвердите почту."));

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "testuser",
                "password", "secret123"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    // ✅ Тест 5: Confirm — успех
    @Test
    void confirm_success() throws Exception {
        when(authService.confirm(anyString(), anyString())).thenReturn("testuser");

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "testuser",
                "code", "1234"
        ));

        mockMvc.perform(post("/api/auth/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("user seccessfully confirmed"));
    }

    // ✅ Тест 6: Refresh — успех
    @Test
    void refresh_success() throws Exception {
        TokenResponse fakeToken = new TokenResponse("new-access-token", "new-refresh-token");
        when(authService.refresh(anyString())).thenReturn(fakeToken);

        String body = objectMapper.writeValueAsString(Map.of(
                "refreshToken", "old-refresh-token"
        ));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"));
    }

    // ✅ Тест 7: Logout — успех
    @Test
    void logout_success() throws Exception {
        doNothing().when(authService).logout(anyString());

        String body = objectMapper.writeValueAsString(Map.of(
                "refreshToken", "some-refresh-token"
        ));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ✅ Тест 8: Resend — успех
    @Test
    void resend_success() throws Exception {
        doNothing().when(authService).sendCodeAgain(anyString());

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "test@example.com"
        ));

        mockMvc.perform(post("/api/auth/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("code to test@example.com was seccessfully send"));
    }
}