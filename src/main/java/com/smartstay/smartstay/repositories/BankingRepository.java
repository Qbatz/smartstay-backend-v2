package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingRepository extends JpaRepository<BankingV1, String> {

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.accountType = :type and b.hostelId=:hostelId and b.userId=:userId")
    List<String> findBankIdsByAccountTypeAndHostelIdAndUserId(String type, String hostelId, String userId);

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.accountNumber = :accountNumber and b.hostelId=:hostelId and b.userId=:userId")
    List<String> findBankIdsByAccountNumberAndHostelIdAndUserId(String accountNumber, String hostelId, String userId);

    @Query("SELECT b.bankId FROM BankingV1 b " + "WHERE b.accountNumber = :accountNumber AND b.accountType = :type " + "AND b.bankId <> :excludedBankId")
    List<String> findBankIdsByAccountNumberAndAccountTypeNotEqualBankId(@Param("accountNumber") String accountNumber, @Param("type") String type, @Param("excludedBankId") String excludedBankId);
    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.upiId = :upiId AND b.accountType = :type and b.hostelId=:hostelId and b.userId=:userId")
    List<String> findBankIdsByUpiIdAndAccountType(String upiId, String type, String hostelId, String userId);

    @Query("SELECT b.bankId FROM BankingV1 b " +
           "WHERE b.upiId = :upiId AND b.accountType = :type " +
           "AND b.bankId <> :excludedBankId")
    List<String> findBankIdsByUpiIdAndAccountTypeNotEqualBankId(@Param("upiId") String upiId,
                                                                @Param("type") String type,
                                                                @Param("excludedBankId") String excludedBankId);

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.creditCardNumber = :creditCardNumber AND b.accountType = :type and b.hostelId=:hostelId and b.userId=:userId")
    List<String> findBankIdsByCreditCardAndAccountType(String creditCardNumber, String type, String hostelId, String userId);

    @Query("SELECT b.bankId FROM BankingV1 b " +
           "WHERE b.creditCardNumber = :creditCardNumber AND b.accountType = :type " +
           "AND b.bankId <> :excludedBankId")
    List<String> findBankIdsByCreditCardAndAccountTypeNotEqualBankId(@Param("creditCardNumber") String creditCardNumber,
                                                                     @Param("type") String type,
                                                                     @Param("excludedBankId") String excludedBankId);

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.debitCardNumber = :debitCardNumber AND b.accountType = :type and b.hostelId=:hostelId and b.userId=:userId")
    List<String> findBankIdsByDebitCardAndAccountType(String debitCardNumber, String type, String hostelId, String userId);

    @Query("SELECT b.bankId FROM BankingV1 b " +
           "WHERE b.debitCardNumber = :debitCardNumber AND b.accountType = :type " +
           "AND b.bankId <> :excludedBankId")
    List<String> findBankIdsByDebitCardAndAccountTypeNotEqualBankId(@Param("debitCardNumber") String debitCardNumber,
                                                                    @Param("type") String type,
                                                                    @Param("excludedBankId") String excludedBankId);


    List<BankingV1> findByBankIdIn(List<String> bankIds);
    List<BankingV1> findByHostelId(String hostelId);

    BankingV1 findByBankIdInAndIsDefaultAccountTrue(List<String> bankIds);
    BankingV1 findByHostelIdAndIsDefaultAccountTrue(String hostelId);
    BankingV1 findByBankId(String bankId);

    @Query("SELECT COUNT(b) > 0 " +
           "FROM BankingV1 b " +
           "WHERE b.hostelId = :hostelId AND b.bankId = :bankId AND b.isDeleted = false AND b.isActive = true")
    boolean existsByHostelIdAndBankId(@Param("hostelId") String hostelId, @Param("bankId") String bankId);

    @Query("SELECT b FROM BankingV1 b WHERE b.hostelId = :hostelId AND b.bankId = :bankId AND b.isDeleted = false AND b.isActive = true")
    BankingV1 findBankingRecordByHostelIdAndBankId(@Param("hostelId") String hostelId, @Param("bankId") String bankId);

    @Query(value = """
            SELECT * FROM bankingv1 banking where is_active=true and transaction_type in ('BOTH', 'CREDIT') and 
            bank_id in (:bankIds)
            """, nativeQuery = true)
    List<BankingV1> findByBankIdInAndActiveAccount(List<String> bankIds);

    @Query(value = """
            SELECT * FROM bankingv1 banking where is_active=true and transaction_type in ('BOTH', 'DEBIT') and 
            hostel_id=:hostelId
            """, nativeQuery = true)
    List<BankingV1> findByBankIdInAndActiveAccountDebit(@Param("hostelId")  String hostelId);

    List<BankingV1> findByParentId(String parentId);

}
