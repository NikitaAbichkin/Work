package com.example.auth.service;

import com.example.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Slf4j
public class CleanupService {
    private final UserRepository userRepository;

    public CleanupService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredUnconfirmedUsers(){
        log.info("Scheduled cleanup started: deleting expired unconfirmed users");
        userRepository.deleteExpiredUnconfirmedUsers();
        log.info("Scheduled cleanup finished: deleteExpiredUnconfirmedUsers executed");
    }
}
// тут прописываем что делать каждый временной промежуток
// fixedRate = 3600000 сказали что каждый час будем вызывать метод deleteExpiredUnconfirmedUsers