package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingMethods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingMethodsRepository extends JpaRepository<BankingMethods, String> {

    List<BankingMethods> findByBank_BankIdOrderByCreatedAtAsc(String bankId);

    List<BankingMethods> findByBank_BankIdIn(List<String> bankIds);
    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.paymentMethodId=:methodId AND (bm.isDeleted IS NULL OR bm.isDeleted=false)
            """)
    BankingMethods findByMethodId(String methodId);

    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.hostelId=:hostelId AND bm.paymentMethod IN ('CREDIT_CARD', 'DEBIT_CARD') 
            AND bm.cardNumber=:cardNumber AND (bm.isDeleted=false OR bm.isDeleted IS NULL)
            """)
    BankingMethods findByHostelIdAndCardNumber(String hostelId, String cardNumber);
    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.paymentMethodId=:paymentMethodId
            """)
    BankingMethods findByPaymentMethodId(String paymentMethodId);

    boolean existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(String bankId, String hostelId, String upiId);

    boolean existsByBank_BankIdAndHostelIdAndCardNumber(String bankId, String hostelId, String cardNumber);

    @Query("""
            SELECT bm FROM BankingMethods bm WHERE bm.hostelId=:hostelId AND (bm.isDeleted IS NULL OR bm.isDeleted=false)
            """)
    List<BankingMethods> findAllPaymentMethods(String hostelId);

    @Query("SELECT bm FROM BankingMethods bm WHERE bm.bank.bankId = :bankId AND (bm.isDeleted IS NULL OR bm.isDeleted=false)")
    List<BankingMethods> findMethodsByBankId(@Param("bankId") String bankId);
}
