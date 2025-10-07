package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CreditDebitNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditDebitNotesRepository extends JpaRepository<CreditDebitNotes, Integer> {
}
