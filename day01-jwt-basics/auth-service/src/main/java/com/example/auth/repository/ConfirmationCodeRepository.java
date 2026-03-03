package com.example.auth.repository;
import com.example.auth.model.ConfirmationCode;
import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode,Long> {
    Optional<ConfirmationCode> findByCodeAndUser(String code, User user);

    Optional<ConfirmationCode> deleteByUser(User user);

}
