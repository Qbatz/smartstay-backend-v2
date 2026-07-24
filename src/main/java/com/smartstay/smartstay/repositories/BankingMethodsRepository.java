package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingMethods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BankingMethodsRepository extends JpaRepository<BankingMethods, String> {

    List<BankingMethods> findByBank_BankIdOrderByCreatedAtAsc(String bankId);

    List<BankingMethods> findByBank_BankIdIn(List<String> bankIds);

    boolean existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(String bankId, String hostelId, String upiId);

    boolean existsByBank_BankIdAndHostelIdAndCardNumber(String bankId, String hostelId, String cardNumber);

    @Modifying
    @Query("UPDATE BankingMethods m SET m.balance = COALESCE(m.balance, 0.0) + :amount, " +
            "m.updatedAt = :now, m.updatedBy = :userId WHERE m.paymentMethodId = :paymentMethodId")
    int addBalance(@Param("paymentMethodId") String paymentMethodId, @Param("amount") double amount,
            @Param("now") Date now, @Param("userId") String userId);
}
