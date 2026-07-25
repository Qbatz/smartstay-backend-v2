package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.QrBankType;
import com.smartstay.smartstay.ennum.QrType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrBankTypeRepository extends JpaRepository<QrBankType, Integer> {

    List<QrBankType> findAllByOrderByTypeAscNameAsc();

    List<QrBankType> findAllByTypeOrderByNameAsc(QrType type);

    boolean existsByTypeAndNameIgnoreCase(QrType type, String name);

    boolean existsByTypeAndNameIgnoreCaseAndIdNot(QrType type, String name, Integer id);
}
