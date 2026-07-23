package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingMethods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingMethodsRepository extends JpaRepository<BankingMethods, String> {

    List<BankingMethods> findByBank_BankIdOrderByCreatedAtAsc(String bankId);

    boolean existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(String bankId, String hostelId, String upiId);

    boolean existsByBank_BankIdAndHostelIdAndCardNumber(String bankId, String hostelId, String cardNumber);
}
