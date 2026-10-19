package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankingV2Repository extends JpaRepository<BankingV2, String> {

    @Query("SELECT b FROM bankingv2 b WHERE b.hostelId = :hostelId AND b.isDeleted = false " +
            "ORDER BY b.createdAt DESC")
    List<BankingV2> findBanksByHostelId(@Param("hostelId") String hostelId);

    boolean existsByHostelIdAndAccountNumberAndIsDeletedFalse(String hostelId, String accountNumber);

    List<BankingV2> findByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);

//    @Query("""
//           SELECT b FROM bankingv2 b WHERE b.bankId =:bankId
//            """)
//    BankingV2 findByBankId(String bankId);

    @Query("""
        SELECT b
        FROM bankingv2 b
        LEFT JOIN FETCH b.bankingMethods
        WHERE b.bankId = :bankId
    """)
    Optional<BankingV2> findByBankId(
            @Param("bankId") String bankId
    );

    @Query("""
            SELECT b FROM bankingv2 b WHERE b.accountType='BANK'
            """)
    List<BankingV2> findAllBankAccount();

    @Query("""
    SELECT DISTINCT b
    FROM bankingv2 b
    LEFT JOIN FETCH b.bankingMethods
    WHERE b.hostelId = :hostelId
      AND b.isDeleted = false
""")
    List<BankingV2> findAllByHostelIdWithBankingMethods(
            @Param("hostelId") String hostelId
    );

    @Query("""
    SELECT DISTINCT b
    FROM bankingv2 b
    LEFT JOIN FETCH b.bankingMethods
    WHERE b.bankId IN (:bankIds)
      AND b.isDeleted = false
""")
    List<BankingV2> findAllByBankIds(
            @Param("bankIds") List<String> bankIds
    );

    @Query("""
            SELECT b FROM bankingv2 b LEFT JOIN FETCH b.bankingMethods WHERE b.accountType='CARD' 
            AND b.bankAccountType='CREDIT_CARD' 
            AND b.hostelId=:hostelId AND b.isDeleted=false
            """)
    List<BankingV2> findAllCreditCardByHostelId(String hostelId);
}
