package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Units;
import com.smartstay.smartstay.responses.expenses.UnitResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitsRepository extends JpaRepository<Units, Integer> {

    Units findByUnitId(int unitId);

    Units findByUnitNameIgnoreCase(String unitName);

    @Query("SELECT new com.smartstay.smartstay.responses.expenses.UnitResponse(" +
            "u.unitId, u.unitName) " +
            "FROM Units u " +
            "WHERE u.isEnabled = true " +
            "ORDER BY u.unitId DESC")
    List<UnitResponse> findAllEnabledUnits();
}
