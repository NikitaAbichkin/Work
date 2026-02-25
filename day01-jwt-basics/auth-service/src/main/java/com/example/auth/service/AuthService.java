package com.example.auth.service;



import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }
    public String  register (String username, String password){

        if (userRepository.findUserByUsername(username).isPresent()){
            throw  new RuntimeException("Username already exists");
        } /* тут просто проверка на то что есть ли вообще юзер */


        String hashed_password = passwordEncoder.encode(password);

        User user =  new User();
        user.setUsername(username);
        user.setHashedPassword(hashed_password);

        userRepository.save(user);

        return  username;
    }

    public String login(String username, String password){

        User  user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        String hashedPassword = user.getHashedPassword();


        Boolean isOurToken = passwordEncoder.matches(password,hashedPassword);

        if (!isOurToken)
            throw new RuntimeException("Неверный пароль");

        String token  =  jwtService.generateToken(username, user.getId());


        return token;
    }

}
