package com.example.auth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
//        SpringApplication.run(AuthApplication.class, args);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            result.append(i + " ");
        }






    }
}

