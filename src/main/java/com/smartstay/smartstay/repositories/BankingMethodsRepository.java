package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingMethods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingMethodsRepository extends JpaRepository<BankingMethods, String> {

    List<BankingMethods> findByBank_BankIdOrderByCreatedAtAsc(String bankId);

    List<BankingMethods> findByBank_BankIdIn(List<String> bankIds);
    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.paymentMethodId=:methodId
            """)
    BankingMethods findByMethodId(String methodId);

    boolean existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(String bankId, String hostelId, String upiId);

    boolean existsByBank_BankIdAndHostelIdAndCardNumber(String bankId, String hostelId, String cardNumber);

    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.hostelId=:hostelId AND (bm.isDeleted IS NULL OR bm.isDeleted=false)
            """)
    List<BankingMethods> findAllPaymentMethods(String hostelId);
}
