package com.example.auth.repository;
import com.example.auth.model.RefreshToken;
import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository  extends  JpaRepository<RefreshToken,Long>{
    Optional<RefreshToken> findByToken(String token);

    void  deleteByUser(User user);

    List<RefreshToken> findAllByUser(User user);
    void deleteAllByUser(User user);

}
