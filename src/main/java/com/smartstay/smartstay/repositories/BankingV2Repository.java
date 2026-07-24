package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BankingV2Repository extends JpaRepository<BankingV2, String> {

    @Query(value = "SELECT b FROM BankingV2 b WHERE b.hostelId = :hostelId AND b.isDeleted = false " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BankingV2 b WHERE b.hostelId = :hostelId AND b.isDeleted = false")
    Page<BankingV2> findBanksByHostelId(@Param("hostelId") String hostelId, Pageable pageable);

    boolean existsByHostelIdAndAccountNumberAndIsDeletedFalse(String hostelId, String accountNumber);

    List<BankingV2> findByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);

    @Modifying
    @Query("UPDATE BankingV2 b SET b.balance = COALESCE(b.balance, 0.0) + :amount, " +
            "b.updatedAt = :now, b.updatedBy = :userId WHERE b.bankId = :bankId")
    int addBalance(@Param("bankId") String bankId, @Param("amount") double amount,
            @Param("now") Date now, @Param("userId") String userId);
}
