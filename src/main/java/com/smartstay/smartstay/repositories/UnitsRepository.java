package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Units;
import com.smartstay.smartstay.responses.expenses.UnitResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnitsRepository extends JpaRepository<Units, Integer> {

    Units findByUnitId(int unitId);

    Units findByUnitIdAndHostelId(int unitId, String hostelId);

    Units findByUnitNameIgnoreCaseAndHostelId(String unitName, String hostelId);

    @Query("SELECT new com.smartstay.smartstay.responses.expenses.UnitResponse(" +
            "u.unitId, u.unitName) " +
            "FROM Units u " +
            "WHERE u.isEnabled = true AND u.hostelId = :hostelId " +
            "ORDER BY u.unitId DESC")
    List<UnitResponse> findAllEnabledUnitsByHostelId(@Param("hostelId") String hostelId);
}
