package com.ezbuy.ezbuy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ezbuy.ezbuy.entities.Promotion;

import java.time.LocalDateTime;
import java.util.Optional;
public interface PromotionRepository extends JpaRepository<Promotion, Integer>, JpaSpecificationExecutor<Promotion> {
    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);

    @Query("SELECT p FROM Promotion p WHERE p.code = :code " +
           "AND p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :now) " +
           "AND (p.endDate IS NULL OR p.endDate >= :now)")
    Optional<Promotion> findValidPromotionByCode(
            @Param("code") String code, 
            @Param("now") LocalDateTime now
    );
}