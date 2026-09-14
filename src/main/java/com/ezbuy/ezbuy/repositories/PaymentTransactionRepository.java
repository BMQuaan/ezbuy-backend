package com.ezbuy.ezbuy.repositories;

import com.ezbuy.ezbuy.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    List<PaymentTransaction> findByOrderId(Integer orderId);
    Optional<PaymentTransaction> findByTxnRef(String txnRef);
    Optional<PaymentTransaction> findByTransactionNo(String transactionNo);
}
