package com.ezbuy.ezbuy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ezbuy.ezbuy.entities.Token;
import com.ezbuy.ezbuy.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("select t from Token t where t.user = :user and t.revoked = false")
    List<Token> findByUserAndRevokedFalse(User user);

    Optional<Token> findByToken(String token);
}