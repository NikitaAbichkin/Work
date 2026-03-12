package com.example.auth.service;

import lombok.extern.slf4j.Slf4j;

import com.example.auth.exception.*;
import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.TokenResponse;
import com.example.auth.model.ConfirmationCode;
import com.example.auth.model.RefreshToken;
import com.example.auth.model.User;
import com.example.auth.repository.ConfirmationCodeRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;

import org.antlr.v4.runtime.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class AuthService {
    private final ResendEmailService resendEmailService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private  final ConfirmationCodeRepository confirmationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    public AuthService(UserRepository userRepository, JwtService jwtService,ConfirmationCodeRepository confirmationCodeRepository, ResendEmailService resendEmailService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.jwtService = jwtService;
        this.confirmationCodeRepository =  confirmationCodeRepository;
        this.resendEmailService = resendEmailService;
        this.refreshTokenRepository = refreshTokenRepository;

    }
    @Transactional
    public String  register (String username, String password, String email){
        log.info("Register request received for username={} email={}", username, email);

        if (userRepository.findUserByUsername(username).isPresent()){
            log.warn("Register failed: username={} already exists", username);
            // В ТЗ нет отдельного кода для конфликта по username, поэтому оставляем общее поведение
            throw  new RuntimeException("Username already exists");
        } /* тут просто проверка на то что есть ли вообще юзер */


        String hashedPassword = passwordEncoder.encode(password);

        User user =  new User();
        user.setUsername(username);
        user.setHashedPassword(hashedPassword);
        user.setEmail(email);

        userRepository.save(user);


        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));

        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setCode(code);
        confirmationCode.setUser(user);
        confirmationCodeRepository.save(confirmationCode);
        log.debug("Confirmation code generated for username={}", username);
        resendEmailService.sendCode(email,code);
        log.info("User registered successfully: {}", username);
        return  username;
            
    }
    @Transactional
    public TokenResponse login(String username, String password){
        log.info("Login request received for username={}", username);

        User  user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for username={}", username);
                    return new UserNotFoundException();
                });
        String hashedPassword = user.getHashedPassword();
        if ("NOT_CONFIRMED".equals(user.getStatus())) {
            log.warn("Login failed: account not confirmed for username={}", username);
            throw new AccountInactiveException();
        }


        Boolean isOurToken = passwordEncoder.matches(password,hashedPassword);

        if (!isOurToken) {
            log.warn("Login failed: invalid credentials for username={}", username);
            throw new InvalidCredentialsException();
        }



        String token  =  jwtService.generateToken(username, user.getId());
        Map<String,String> result = new HashMap<>();

        String RefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(RefreshToken);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        TokenResponse tokenResponse = new TokenResponse(token, RefreshToken);
        log.info("Login successful for username={} userId={}", username, user.getId());
        return  tokenResponse;
    }
    @Transactional
    public  String confirm (String username, String code ){
        log.info("Confirm email request received for username={}", username);

        User  user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Confirm failed: user not found for username={}", username);
                    return new UserNotFoundException();
                });

        ConfirmationCode confirmationCode = confirmationCodeRepository.findByCodeAndUser(code,user)
                .orElseThrow(() -> {
                    log.warn("Confirm failed: invalid confirmation code for username={}", username);
                    return new InvalidTokenException();
                });
        LocalDateTime dateFinish = confirmationCode.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateFinish)){
            log.warn("Confirm failed: token expired for username={}", username);
            throw new TokenExpiredException();
        }
        user.setStatus("ACTIVE");
        userRepository.save(user);
        confirmationCodeRepository.delete(confirmationCode);
        log.info("User email successfully confirmed for username={}", username);

        return username;
    }

    @Transactional
    public  void  sendCodeAgain(String email){
        log.info("Resend confirmation code request received for email={}", email);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Resend confirmation code failed: user not found for email={}", email);
                    return new UserNotFoundException();
                });
        if ("ACTIVE".equals(user.getStatus())){
            log.warn("Resend confirmation code skipped: user already active for email={}", email);
            throw new RuntimeException("Пользователь уже подтвердил почту");
        }

        confirmationCodeRepository.deleteByUser(user);
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setCode(code);
        confirmationCode.setUser(user);



        confirmationCodeRepository.save(confirmationCode);
        log.debug("New confirmation code generated for email={}", email);
        resendEmailService.sendCode(email,code);
        log.info("Confirmation code resent successfully for email={}", email);
    }
    @Transactional
    public  TokenResponse refresh (String refreshToken){
        log.info("Refresh token request received");

        RefreshToken refreshToken1= refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token failed: token not found");
                    return new InvalidRefreshTokenException();
                });

        LocalDateTime dateFinish = refreshToken1.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateFinish)){
            log.warn("Refresh token failed: token expired for userId={}", refreshToken1.getUser().getId());
            throw new InvalidRefreshTokenException();
        }
     User user =   refreshToken1.getUser();
        refreshTokenRepository.delete(refreshToken1);
        RefreshToken refreshToken2  = new RefreshToken();

        String refreshTokenString = jwtService.generateRefreshToken();
        refreshToken2.setUser(user);
        refreshToken2.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken2.setToken(refreshTokenString);

        String token = jwtService.generateToken(user.getUsername(),user.getId());




        refreshTokenRepository.save(refreshToken2);
        TokenResponse tokenResponse = new TokenResponse(token,refreshTokenString);
        log.info("Refresh token successful for userId={}", user.getId());

        return  tokenResponse;
    }
    @Transactional
    public  void logout (String refreshToken){
        log.info("Logout request received");

        RefreshToken Refresh_token  = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> {
            log.warn("Logout failed: refresh token not found");
            return new InvalidRefreshTokenException();
        });
        refreshTokenRepository.delete(Refresh_token);
        log.info("Logout successful for userId={}", Refresh_token.getUser().getId());
    }






}
