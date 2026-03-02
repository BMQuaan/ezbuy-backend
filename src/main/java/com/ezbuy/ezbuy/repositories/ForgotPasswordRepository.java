package com.ezbuy.ezbuy.repositories;

import com.ezbuy.ezbuy.entities.ForgotPassword;
import com.ezbuy.ezbuy.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
    Optional<ForgotPassword> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}