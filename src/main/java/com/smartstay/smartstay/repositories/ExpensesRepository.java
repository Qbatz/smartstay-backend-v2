package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensesV1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpensesRepository extends JpaRepository<ExpensesV1, String> {
}
