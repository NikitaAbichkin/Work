package com.example.auth.repository;
import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface  UserRepository  extends  JpaRepository<User,Long>{
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);

    Optional<User> findById(Long id);

    @Modifying
    @Query("DELETE FROM User u WHERE u.status = 'NOT_CONFIRMED' AND NOT EXISTS (SELECT c FROM ConfirmationCode c WHERE c.user = u AND c.expiresAt > CURRENT_TIMESTAMP)")
    void deleteExpiredUnconfirmedUsers();
    //выполни SQL который над тобой когда тебя вызовут

}



