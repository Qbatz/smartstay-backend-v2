package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingRepository extends JpaRepository<BankingV1, String> {

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.accountNumber = :accountNumber AND b.accountType = :type")
    List<String> findBankIdsByAccountNumberAndAccountType(String accountNumber, String type);


    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.upiId = :upiId AND b.accountType = :type")
    List<String> findBankIdsByUpiIdAndAccountType(String upiId, String type);

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.creditCardNumber = :creditCardNumber AND b.accountType = :type")
    List<String> findBankIdsByCreditCardAndAccountType(String creditCardNumber, String type);

    @Query("SELECT b.bankId FROM BankingV1 b WHERE b.debitCardNumber = :debitCardNumber AND b.accountType = :type")
    List<String> findBankIdsByDebitCardAndAccountType(String debitCardNumber, String type);

    List<BankingV1> findByBankIdIn(List<String> bankIds);

    BankingV1 findByBankIdInAndIsDefaultAccountTrue(List<String> bankIds);

}
