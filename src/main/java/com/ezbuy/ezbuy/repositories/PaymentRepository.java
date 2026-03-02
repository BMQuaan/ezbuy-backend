package com.ezbuy.ezbuy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ezbuy.ezbuy.entities.Payment;
public interface PaymentRepository extends JpaRepository<Payment, Integer> {}