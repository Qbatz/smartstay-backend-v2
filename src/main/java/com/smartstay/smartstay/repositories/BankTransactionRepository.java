package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransactionsV1, Integer> {

    List<BankTransactionsV1> findByBankIdIn(List<String> listBankIds);

    List<BankTransactionsV1> findByHostelIdAndIsDeletedFalseOrderByTransactionDateDesc(String hostelId);

    List<BankTransactionsV1> findByHostelIdAndBankIdInAndIsDeletedFalseOrderByTransactionDateDesc(String hostelId, List<String> bankIds);

    BankTransactionsV1 findTopByBankIdOrderByTransactionDateDesc(String bankId);

    BankTransactionsV1 findByBankIdAndHostelId(String bankId, String hostelId);

    BankTransactionsV1 findTopByBankIdAndHostelIdOrderByCreatedAtDesc(String bankId, String hostelId);

    BankTransactionsV1 findByTransactionNumber(String transactionNumber);

    @Query("SELECT COUNT(bt) FROM BankTransactionsV1 bt WHERE bt.hostelId = :hostelId AND DATE(bt.transactionDate) >= DATE(:startDate) AND DATE(bt.transactionDate) <= DATE(:endDate)")
    int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query("""
            SELECT btv FROM BankTransactionsV1 btv WHERE btv.type='DEBIT'
            """)
    List<BankTransactionsV1> findByTransactionType();

    BankTransactionsV1 findByHostelIdAndSourceId(String hostelId, String sourceId);

    @Query(value = "SELECT t FROM BankTransactionsV1 t WHERE t.hostelId = :hostelId " +
            "AND (t.isDeleted = false OR t.isDeleted IS NULL) " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
            "AND (:source IS NULL OR t.source = :source)",
            countQuery = "SELECT COUNT(t) FROM BankTransactionsV1 t WHERE t.hostelId = :hostelId " +
                    "AND (t.isDeleted = false OR t.isDeleted IS NULL) " +
                    "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
                    "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
                    "AND (:source IS NULL OR t.source = :source)")
    Page<BankTransactionsV1> findTransactions(@Param("hostelId") String hostelId,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate,
            @Param("source") String source, Pageable pageable);

    @Query("SELECT t FROM BankTransactionsV1 t " +
            "WHERE t.hostelId = :hostelId AND t.bankId = :bankId " +
            "AND (t.isDeleted = false OR t.isDeleted IS NULL) " +
            "AND t.createdAt >= :startDate")
    List<BankTransactionsV1> findOverviewTransactions(@Param("hostelId") String hostelId,
            @Param("bankId") String bankId, @Param("startDate") Date startDate);

}
