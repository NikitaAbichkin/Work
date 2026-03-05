package com.example.auth.service;

import com.example.auth.repository.UserRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
public class CleanupService {
    private final UserRepository userRepository;

    public CleanupService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredUnconfirmedUsers(){
        userRepository.deleteExpiredUnconfirmedUsers();
    }
}
// тут прописываем что делать каждый временной промежуток
// fixedRate = 3600000 сказали что каждый час будем вызывать метод deleteExpiredUnconfirmedUsers