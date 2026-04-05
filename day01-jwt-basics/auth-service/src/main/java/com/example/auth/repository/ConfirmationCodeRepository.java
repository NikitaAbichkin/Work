package com.example.auth.repository;
import com.example.auth.model.ConfirmationCode;
import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode,Long> {
    Optional<ConfirmationCode> findByCodeAndUser(String code, User user);

    List<ConfirmationCode> findAllByUser(User user);
    Optional<ConfirmationCode> findTopByUserOrderByCreatedAtDesc(User user);
    Optional<ConfirmationCode> deleteByUser(User user);

}
