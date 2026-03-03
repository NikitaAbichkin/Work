package com.example.auth.service;



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

        if (userRepository.findUserByUsername(username).isPresent()){
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
        resendEmailService.sendCode(email,code);
        return  username;
    }

    public TokenResponse login(String username, String password){

        User  user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        String hashedPassword = user.getHashedPassword();
        if ("NOT_CONFIRMED".equals(user.getStatus())) {
            throw new RuntimeException("Аккаунт не активирован. Подтвердите почту.");
        }


        Boolean isOurToken = passwordEncoder.matches(password,hashedPassword);

        if (!isOurToken)
            throw new RuntimeException("Неверный пароль");



        String token  =  jwtService.generateToken(username, user.getId());
        Map<String,String> result = new HashMap<>();

        String RefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(RefreshToken);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        TokenResponse tokenResponse = new TokenResponse(token, RefreshToken);
        return  tokenResponse;
    }
    public  String confirm (String username, String code ){
        User  user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        ConfirmationCode confirmationCode = confirmationCodeRepository.findByCodeAndUser(code,user)
                .orElseThrow(()->new RuntimeException("Неверный код подтверждения"));
        LocalDateTime dateFinish = confirmationCode.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateFinish)){
            throw new RuntimeException("Код истек");
        }
        user.setStatus("ACTIVE");
        userRepository.save(user);
        confirmationCodeRepository.delete(confirmationCode);

       return username;
    }

    @Transactional
    public  void  sendCodeAgain(String email){
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден") );
        if ("ACTIVE".equals(user.getStatus())){
            throw new RuntimeException("Пользователь уже подтвердил почту");
        }

        confirmationCodeRepository.deleteByUser(user);
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setCode(code);
        confirmationCode.setUser(user);



        confirmationCodeRepository.save(confirmationCode);
        resendEmailService.sendCode(email,code);

    }

    public  TokenResponse refresh (String refreshToken){
         RefreshToken refreshToken1= refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Токен не найден"));

        LocalDateTime dateFinish = refreshToken1.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateFinish)){
            throw new RuntimeException("токен истек");
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

        return  tokenResponse;
    }

    public  void logout (String refreshToken){
         RefreshToken Refresh_token  = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new RuntimeException("Токен не найден"));
        refreshTokenRepository.delete(Refresh_token);


    }






}
